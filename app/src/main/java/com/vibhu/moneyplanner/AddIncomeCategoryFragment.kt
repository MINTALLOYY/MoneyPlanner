package com.vibhu.moneyplanner

import com.vibhu.moneyplanner.models.IncomeCategory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.databinding.ActivityAddIncomeCategoryBinding

class AddIncomeCategoryFragment: Fragment() {

    private lateinit var binding: ActivityAddIncomeCategoryBinding
    private lateinit var incomeCategoryData: IncomeCategoryData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = ActivityAddIncomeCategoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeCategoryData = IncomeCategoryData(requireContext())

        binding.buttonAddIncomeCategory.setOnClickListener {
            val name = binding.editTextIncomeCategoryName.text.toString()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val incomeCategory = IncomeCategory(incomeCategoryName = name)
            incomeCategoryData.addIncomeCategory(incomeCategory)

            goBackToIncomePage("Income category added successfully")
        }

        binding.buttonCancel.setOnClickListener {
            goBackToIncomePage()
        }
    }

    fun goBackToIncomePage(message: String? = null){
        val bundle = Bundle()
        bundle.putString("message", message) //Pass message

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val incomeCategoryFragment = IncomeCategoryFragment()
        incomeCategoryFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, incomeCategoryFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeCategoryData.close()
    }
}