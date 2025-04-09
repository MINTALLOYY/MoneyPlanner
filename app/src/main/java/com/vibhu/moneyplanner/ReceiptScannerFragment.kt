package com.vibhu.moneyplanner

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.textract.AmazonTextract
import com.amazonaws.services.textract.AmazonTextractClient
import com.vibhu.moneyplanner.categoryexpense.AddExpenseFragment
import com.vibhu.moneyplanner.databinding.FragmentReceiptScannerBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.util.UUID


class ReceiptScannerFragment : Fragment() {

    private var _binding: FragmentReceiptScannerBinding? = null
    private val binding get() = _binding!!
    private lateinit var resultTextView: TextView
    private lateinit var textractManager: TextractManager
    private lateinit var categoryId: UUID
    private var total: Float = 0.0F
    private lateinit var filePath: String

    private val textractClient: AmazonTextract by lazy {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            "us-east-1:250834a1-31f1-4bff-a8ac-adfd1484a595", // Replace with your Identity Pool ID
            Regions.US_EAST_1 // Replace with your region (e.g., Regions.US_EAST_1)
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
        if (filePathTest != null) {
            filePath = filePathTest
        } else{
            goToHomePage("No File Found")
        }
        if (categoryIdString!= null) {
            categoryId = UUID.fromString(categoryIdString)
        }


        val tempFile = File(filePath)

        textractManager.analyzeDocument(tempFile) { extractedText, error ->
            if (extractedText != null) {
                requireActivity().runOnUiThread { // Switch to main thread
                    Log.d("Textract Result", extractedText)
                    resultTextView.text = extractedText
                    total = getRidOfCurrencySymbol(extractedText)
                    enableAddingExpense()
                }
            } else if (error != null) {
                Log.e("Textract Error", error.message.toString())
                // Handle error
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

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val addExpenseFragment = AddExpenseFragment()
        addExpenseFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, addExpenseFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun goToHomePage(message: String){
        val bundle = Bundle()
        bundle.putString("message", message)

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val homeFragment = HomeFragment()
        homeFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, homeFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}