package com.client.moviezz.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.client.moviezz.R
import com.client.moviezz.adapters.EpisodeListAdapter
import com.client.moviezz.models.SubVideo
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest

@Suppress("DEPRECATION")
class DanhSachTapFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var episodeAdapter: EpisodeListAdapter
    private lateinit var viewModel: MovieViewModel
    private var onEpisodeClick: ((SubVideo) -> Unit)? = null
    private lateinit var sharedEpisodeViewModel: MovieViewModel.SharedEpisodeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_danh_sach_tap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gán ViewModel từ activity
        viewModel = ViewModelProvider(requireActivity())[MovieViewModel::class.java]

        recyclerView = view.findViewById(R.id.recycler_view_danh_sach_tap)
        episodeAdapter = EpisodeListAdapter()
        recyclerView.adapter = episodeAdapter
//        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sharedEpisodeViewModel = ViewModelProvider(requireActivity())[MovieViewModel.SharedEpisodeViewModel::class.java]

//        sharedEpisodeViewModel.selectedEpisode.observe(viewLifecycleOwner) { episode ->
//            episodeAdapter.setSelectedEpisode(episode)
//        }

        // Xử lý sự kiện click tập phim
        episodeAdapter.onItemClick = { subVideo ->
            Log.d("DanhSachTapFragment", "Episode clicked: ${subVideo.episode}, link: ${subVideo.link}")
            onEpisodeClick?.invoke(subVideo)
            sharedEpisodeViewModel.selectEpisode(subVideo)
        }

        // Lắng nghe dữ liệu filmDetail và cập nhật adapter
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.filmDetail.collectLatest { filmDetail ->
                filmDetail?.subVideoList?.let { episodes ->
                    Log.d("DanhSachTapFragment", "Submitting ${episodes.size} episodes: $episodes")
                    episodeAdapter.submitList(episodes)
                }
            }
        }
    }

    // Hàm để nhận callback từ DetailAdapter
    fun setOnEpisodeClickListener(listener: ((SubVideo) -> Unit)?) {
        this.onEpisodeClick = listener
    }

    // Hàm được gọi khi có tập mới được chọn
    fun onEpisodeSelected(index: Int) {
        Log.d("hoho", "Episode selected at index: $index")
        val episodes = viewModel.filmDetail.value?.subVideoList ?: emptyList()
        val sortepisodes = episodes.sortedBy { it.episode }
        if (index in sortepisodes.indices) {
            val selectedEpisode = sortepisodes[index]
            episodeAdapter.setSelectedEpisode(selectedEpisode)
            recyclerView.scrollToPosition(index)
        }

    }
}