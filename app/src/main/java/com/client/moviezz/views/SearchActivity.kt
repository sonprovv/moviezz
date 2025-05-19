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
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var ivBack: ImageView
    private lateinit var ivDelete: ImageView
    private lateinit var edtSearch: EditText
    private lateinit var viewModel: MovieViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovieSearchAdapter
    private lateinit var ivNoFilm: ImageView

    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        anhXa()

        adapter = MovieSearchAdapter()
        recyclerView.adapter = adapter
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        ivBack.setOnClickListener {
            finish()
        }

        ivDelete.setOnClickListener {
            edtSearch.text.clear()
        }

        edtSearch.addTextChangedListener {
            searchQuery.value = it.toString().trim()
        }

        // Lắng nghe keyword gõ vào
        lifecycleScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { keyword ->
                    if (keyword.isNotEmpty()) {
                        recyclerView.visibility = RecyclerView.VISIBLE
                        ivNoFilm.visibility = ImageView.GONE
                        viewModel.fetchSearchFilms(keyword, "")

                    } else {
                        recyclerView.visibility = RecyclerView.GONE
                        ivNoFilm.visibility = ImageView.VISIBLE
                        adapter.submitList(emptyList())
                    }
                }
        }

        // Lắng nghe dữ liệu trả về từ ViewModel
        lifecycleScope.launch {
            viewModel.searchFilms.collectLatest { list ->
                adapter.submitList(list)
                if (list.isEmpty()) {
                    recyclerView.visibility = RecyclerView.GONE
                    ivNoFilm.visibility = ImageView.VISIBLE
                } else {
                    recyclerView.visibility = RecyclerView.VISIBLE

                }
            }
        }
    }

    private fun anhXa() {
        ivNoFilm = findViewById(R.id.iv_no_film)
        ivBack = findViewById(R.id.iv_back)
        ivDelete = findViewById(R.id.iv_delete)
        edtSearch = findViewById(R.id.edt_search)
        recyclerView = findViewById(R.id.recycler_view_film_search)
    }
}
