package com.vibhu.moneyplanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryData: CategoryData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryData = CategoryData(this)

        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(this)

        categoryAdapter = CategoryAdapter(categoryData.getAllCategories()) { categoryId ->
            val intent = Intent(this, CategoryExpensesActivity::class.java)
            intent.putExtra(CategoryExpensesActivity.EXTRA_CATEGORY_ID, categoryId.toString())
            startActivity(intent)
        }
        binding.recyclerViewCategories.adapter = categoryAdapter


        binding.buttonAddCategory.setOnClickListener {
            val intent = Intent(this, AddCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        categoryAdapter.updateCategories(categoryData.getAllCategories())
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryData.close()
    }
}