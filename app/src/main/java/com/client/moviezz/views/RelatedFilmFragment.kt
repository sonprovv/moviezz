package com.client.moviezz.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.client.moviezz.R
import com.client.moviezz.adapters.RelatedFilmAdapter
import com.client.moviezz.models.Film
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RelatedFilmFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RelatedFilmAdapter
    private lateinit var viewModel: MovieViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_related_film, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyler_view_related_film_of_category)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = RelatedFilmAdapter()
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[MovieViewModel::class.java]

        // Gọi API khi có filmDetail
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filmDetail.collectLatest { filmDetail ->
                filmDetail?.let {
                    viewModel.fetchRelatedFilms(it.categoryId, "")
                }
            }
        }

        // Lắng nghe dữ liệu phim liên quan
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.relatedFilms.collectLatest { relatedList ->
                val currentFilmId = viewModel.filmDetail.value?.id
                val filteredList = relatedList.filter { it.id != currentFilmId }
                adapter.submitList(filteredList)
            }
        }
    }
}
