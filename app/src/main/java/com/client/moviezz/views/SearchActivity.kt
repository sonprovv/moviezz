package com.client.moviezz.views

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.client.moviezz.R
import com.client.moviezz.adapters.MovieSearchAdapter
import com.client.moviezz.databinding.ActivitySearchBinding
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var adapter: MovieSearchAdapter
    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MovieSearchAdapter()
        binding.recyclerViewFilmSearch.adapter = adapter
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivDelete.setOnClickListener {
            binding.edtSearch.text.clear()
        }

        binding.edtSearch.addTextChangedListener {
            searchQuery.value = it.toString().trim()
        }

        // Lắng nghe keyword gõ vào
        lifecycleScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { keyword ->
                    if (keyword.isNotEmpty()) {
                        binding.recyclerViewFilmSearch.visibility = RecyclerView.VISIBLE
                        binding.ivNoFilm.visibility = ImageView.GONE
                        viewModel.fetchSearchFilms(keyword, "")
                    } else {
                        binding.recyclerViewFilmSearch.visibility = RecyclerView.GONE
                        binding.ivNoFilm.visibility = ImageView.VISIBLE
                        adapter.submitList(emptyList())
                    }
                }
        }

        // Lắng nghe dữ liệu trả về từ ViewModel
        lifecycleScope.launch {
            viewModel.searchFilms.collectLatest { list ->
                adapter.submitList(list)
                if (list.isEmpty()) {
                    binding.recyclerViewFilmSearch.visibility = RecyclerView.GONE
                    binding.ivNoFilm.visibility = ImageView.VISIBLE
                } else {
                    binding.recyclerViewFilmSearch.visibility = RecyclerView.VISIBLE
                }
            }
        }
    }
}
