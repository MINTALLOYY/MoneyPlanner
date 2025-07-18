package com.vibhu.moneyplanner.uiFragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import coil.load
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.textract.AmazonTextract
import com.amazonaws.services.textract.AmazonTextractClient
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.TextractManager
import com.vibhu.moneyplanner.addingData.AddExpenseFragment
import com.vibhu.moneyplanner.databinding.FragmentReceiptScannerBinding
import java.io.File
import java.util.UUID

class ReceiptScannerFragment : Fragment() {

    private var _binding: FragmentReceiptScannerBinding? = null
    private val binding get() = _binding!!
    private lateinit var resultTextView: TextView
    private lateinit var textractManager: TextractManager
    private lateinit var categoryId: UUID
    private var total: Float = 0.0F
    private var name: String? = null
    private var dateStr: String? = null
    private lateinit var filePath: String
    private lateinit var photoUri: Uri

    private val textractClient: AmazonTextract by lazy {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            "us-east-1:250834a1-31f1-4bff-a8ac-adfd1484a595", // Identity Pool ID
            Regions.US_EAST_1
        )
        AmazonTextractClient(credentialsProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReceiptScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resultTextView = binding.receiptTotal
        textractManager = TextractManager(textractClient)

        val categoryIdString = arguments?.getString("category_id")
        val filePathTest = arguments?.getString("filePath")
        val photoUriString = arguments?.getString("photoUri")

        if (filePathTest != null && photoUriString != null) {
            filePath = filePathTest
            photoUri = photoUriString.toUri()
            val imageView = binding.receipt
            Log.d("CameraActivity", "Photo URI: $photoUri")
            imageView.load(photoUri)
        } else{
            goToExpensesPage("No File Found")
        }
        if (categoryIdString!= null) {
            categoryId = UUID.fromString(categoryIdString)
        }


        val tempFile = File(filePath)

        textractManager.analyzeDocument(tempFile) { extractedName, extractedDate, extractedTotal , error ->

            if (extractedTotal != null ) {
                requireActivity().runOnUiThread { // Switch to main thread
                    Log.d("Textract Result", "Total: $extractedTotal, Name: $extractedName, Date: $extractedDate")
                    resultTextView.text = extractedTotal
                    total = getRidOfCurrencySymbol(extractedTotal)
                    if(extractedName != null) name = extractedName
                    if(extractedDate != null) dateStr = extractedDate

                    enableAddingExpense()
                }
            } else if (error != null) {
                Log.e("Textract Error", error.message.toString())
                goToExpensesPage("Cannot Find Total of Receipt")
                // Handle error
            } else {
                Log.e("Textract Error", "Cannot find total or receipt is invalid")
                goToExpensesPage("Cannot Find Total of Receipt")
            }
            Log.d("Analyzing Over", "ANALYZING IS OVER")
        }
    }

    fun getRidOfCurrencySymbol(dollar: String) : Float{
        val moneyAmount = dollar.replace("$", "").replace("€", "").replace("£", "")
        return moneyAmount.toFloat()
    }

    fun enableAddingExpense(){

        binding.addTotalButton.setOnClickListener{
            goToAddExpenseAutoFill()
        }
    }

    fun goToAddExpenseAutoFill(){
        val bundle = Bundle()
        bundle.putString("categoryId", categoryId.toString()) // Pass categoryId
        bundle.putString("total", total.toString())
        if(name != null) bundle.putString("name", name) else bundle.putString("nameError", "Expense Name Not Found")
        if(dateStr != null) bundle.putString("date", dateStr) else bundle.putString("dateError", "Date Not Found")

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val addExpenseFragment = AddExpenseFragment()
        addExpenseFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, addExpenseFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun goToExpensesPage(message: String){
        val bundle = Bundle()
        bundle.putString("message", message)
        bundle.putString("categoryId", categoryId.toString())

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val expensesFragment = ExpensesFragment()
        expensesFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, expensesFragment)
        fragmentTransaction.commit()
    }
}