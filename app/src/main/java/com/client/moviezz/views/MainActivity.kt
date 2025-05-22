package com.client.moviezz.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.client.moviezz.adapters.CategoryAdapter
import com.client.moviezz.adapters.HistoryWatchMovieAdapter
import com.client.moviezz.adapters.ViewPagerAdapter
import com.client.moviezz.databinding.ActivityMainBinding
import com.client.moviezz.db.room.AppDatabase
import com.client.moviezz.models.Category
import com.client.moviezz.models.PhotoViewPager
import com.client.moviezz.repository.WatchHistoryRepository
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest
import me.relex.circleindicator.CircleIndicator3

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var historyRepository: WatchHistoryRepository
    private var photoList: List<PhotoViewPager> = emptyList()
    private var categoryList: List<Category> = emptyList()
    private var categoryListFilter: List<Category> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (photoList.isNotEmpty()) {
                val currentItem = binding.viewPagerBander.currentItem
                if (currentItem == photoList.size - 1) {
                    binding.viewPagerBander.setCurrentItem(0, true)
                } else {
                    binding.viewPagerBander.setCurrentItem(currentItem + 1, true)
                }
                handler.postDelayed(this, 3000)
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Initialize adapters
        categoryAdapter = CategoryAdapter()
        binding.recyclerViewCategory.adapter = categoryAdapter

        binding.ivLogout.setOnClickListener {
            // Create confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đồng ý") { dialog, _ ->
                    dialog.dismiss()
                    // Clear any login information if necessary
                    // Navigate back to login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }

        historyRepository = WatchHistoryRepository(AppDatabase.invoke(this))
        val historyWatchMovieadapter = HistoryWatchMovieAdapter(
            onItemClick = { movie ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("movie_id", movie.movieId)
                    putExtra("seek_time", movie.lastPosition)
                    putExtra("movie_title", movie.movieTitle)
                    putExtra("movie_image", movie.movieImage)
                    putExtra("video_link", movie.videoLink)
                    putExtra("film_id", movie.movieId.toIntOrNull() ?: 0)
                    putExtra("film_avatar", movie.movieImage)
                    putExtra("episode_number", movie.episodeNumber)
                }
                startActivity(intent)
            },
            historyRepository = historyRepository
        )
        Log.e("hoho", "historyWatchMovieadapter: " + historyWatchMovieadapter)
        binding.recyclerViewRecentlyWatched.adapter = historyWatchMovieadapter
        binding.recyclerViewRecentlyWatched.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        // Initialize ViewPagerAdapter with click listener
        viewPagerAdapter = ViewPagerAdapter(
            photoList = emptyList(),
            onItemClick = { photo ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("movie_id", photo.id.toString())
                    putExtra("film_id", photo.id)
                    putExtra("film_avatar", photo.avatar)
                }
                startActivity(intent)
            }
        )
        binding.viewPagerBander.adapter = viewPagerAdapter
        binding.viewPagerBander.offscreenPageLimit = 2
        binding.circleMain.setViewPager(binding.viewPagerBander)

        // Handle search click
        binding.ivSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // ViewPager callback
        binding.viewPagerBander.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                if (photoList.isNotEmpty()) {
                    handler.postDelayed(runnable, 3000)
                }
            }
        })

        // Collect history movies from Room
        lifecycleScope.launchWhenStarted {
            historyRepository.getAllHistory().collectLatest { historyMovies ->
                Log.d("MainActivity", "Received ${historyMovies.size} history movies")
                historyWatchMovieadapter.submitList(historyMovies)
            }
        }

        // Collect movies data from ViewModel
        lifecycleScope.launchWhenStarted {
            viewModel.movies.collectLatest { movies ->
                photoList = movies.map { PhotoViewPager(it) }
                Log.d("MainActivity", "Received ${photoList.size} movies")
                viewPagerAdapter.updateData(photoList)
                binding.circleMain.setViewPager(binding.viewPagerBander)
                if (photoList.isNotEmpty()) {
                    handler.postDelayed(runnable, 3000)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.categorys.collectLatest { categories ->
                categoryList = categories
                Log.d("MainActivity", "Received ${categoryList.size} categories")
                // Fetch films for each category after categories are loaded
                if (categoryList.isNotEmpty()) {
                    for (category in categoryList) {
                        viewModel.fetchFilmOfCategory(category.id, "")
                    }
                    categoryListFilter = categoryList.filter { it.films?.isNotEmpty() == true }
                    categoryAdapter.setData(categoryListFilter) // Set initial state with empty film lists
                }
            }
        }

        viewModel.fetchMovies("")
        viewModel.fetchCategoryList("")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}