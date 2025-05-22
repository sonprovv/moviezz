package com.client.moviezz.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.client.moviezz.R
import com.client.moviezz.adapters.DetailAdapter
import com.client.moviezz.adapters.EpisodeFullScreenAdapter
import com.client.moviezz.adapters.EpisodeListAdapter
import com.client.moviezz.adapters.FilmFullscreenAdapter
import com.client.moviezz.adapters.RelatedFilmAdapter
import com.client.moviezz.databinding.ActivityDetailBinding
import com.client.moviezz.db.room.AppDatabase
import com.client.moviezz.db.room.HistoryMovie
import com.client.moviezz.models.Film
import com.client.moviezz.models.FilmDetail
import com.client.moviezz.repository.WatchHistoryRepository
import com.client.moviezz.viewmodel.MovieViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

@Suppress("DEPRECATION")
@UnstableApi
class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var historyRepository: WatchHistoryRepository
    private lateinit var detailViewModel: MovieViewModel.DetailViewModel
    private lateinit var adapter: DetailAdapter
    private lateinit var episodeAdapter: EpisodeListAdapter
    private lateinit var episodeFullScreenAdapter: EpisodeFullScreenAdapter
    private lateinit var relatedFilmAdapter: RelatedFilmAdapter
    private lateinit var filmFullscreenAdapter: FilmFullscreenAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var gestureDetector: GestureDetector

    private var currentMovieTitle = ""
    private var currentMovieImage = ""
    private var currentVideoLink = ""
    private var currentEpisodeNumber = ""
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var playbackPosition = 0L
    private var currentMediaItemIndex = 0
    private var isFullscreen = false
    private var originalContainerHeight = 0
    private var isPhimBo = false
    private var currentFilmId = 0
    private var avatar: String = ""
    private var filmId: Int = 0
    private var isExpanded: Boolean = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let {
            outState.putLong("playback_position", it.currentPosition)
            outState.putInt("current_media_item", it.currentMediaItemIndex)
            outState.putBoolean("play_when_ready", it.playWhenReady)
        }
        outState.putBoolean("is_fullscreen", isFullscreen)
        outState.putInt("current_film_id", currentFilmId)
        outState.putString("current_video_link", currentVideoLink)
        outState.putString("current_episode_number", currentEpisodeNumber)
        Log.d(
            "hoho",
            "Saved state: position=${player?.currentPosition}, fullscreen=$isFullscreen, filmId=$currentFilmId, videoLink=$currentVideoLink, episode=$currentEpisodeNumber"
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        currentMovieTitle = intent.getStringExtra("movie_title") ?: ""
        currentMovieImage = intent.getStringExtra("movie_image") ?: ""
        currentVideoLink = intent.getStringExtra("video_link") ?: ""
        playbackPosition = intent.getLongExtra("seek_time", 0L)
        currentEpisodeNumber = intent.getStringExtra("episode_number") ?: ""
        Log.e(
            "hoho",
            "Received: title=$currentMovieTitle, image=$currentMovieImage, link=$currentVideoLink, seek_time=$playbackPosition, episode=$currentEpisodeNumber"
        )
        filmId = intent.getIntExtra("film_id", 0)
        avatar = intent.getStringExtra("film_avatar") ?: ""
        currentFilmId = filmId
        Log.d("hoho", "Received film_id: $filmId")
        
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playback_position", playbackPosition)
            currentMediaItemIndex = savedInstanceState.getInt("current_media_item", 0)
            playWhenReady = savedInstanceState.getBoolean("play_when_ready", true)
            isFullscreen = savedInstanceState.getBoolean("is_fullscreen", false)
            currentFilmId = savedInstanceState.getInt("current_film_id", filmId)
            currentVideoLink = savedInstanceState.getString("current_video_link", currentVideoLink)
                ?: currentVideoLink
            currentEpisodeNumber =
                savedInstanceState.getString("current_episode_number", currentEpisodeNumber)
                    ?: currentEpisodeNumber
            Log.d(
                "hoho",
                "Restored state: position=$playbackPosition, fullscreen=$isFullscreen, filmId=$currentFilmId, videoLink=$currentVideoLink, episode=$currentEpisodeNumber"
            )
        }
        
        originalContainerHeight = resources.getDimensionPixelSize(R.dimen.player_height)
        setupGestureDetector()
        initializePlayer()
        initControllerViews()
        initTimeBar()
        setupViewModel()
        setupViewPagerAndTabs()
        setupAdapters()
        setupBottomSheet()
        setupRecyclerViews()
        viewModel.fetchFilmDetail(currentFilmId, "")
    }

    private fun setupRecyclerViews() {
        binding.bottomSheet.recyclerViewPhimBo.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.bottomSheet.recyclerViewPhimBo.adapter = episodeFullScreenAdapter
        binding.bottomSheet.recyclerViewPhimBo.setHasFixedSize(true)
        binding.bottomSheet.recyclerViewPhimBo.isNestedScrollingEnabled = true
        binding.bottomSheet.recyclerViewPhimBo.setItemViewCacheSize(20)
        binding.bottomSheet.recyclerViewPhimBo.setHasFixedSize(true)
        binding.bottomSheet.recyclerViewPhimBo.setItemAnimator(null)

        binding.bottomSheet.recyclerViewPhimLe.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.bottomSheet.recyclerViewPhimLe.adapter = filmFullscreenAdapter
        binding.bottomSheet.recyclerViewPhimLe.setHasFixedSize(true)
        binding.bottomSheet.recyclerViewPhimLe.isNestedScrollingEnabled = true
        binding.bottomSheet.recyclerViewPhimLe.setItemViewCacheSize(20)
        binding.bottomSheet.recyclerViewPhimLe.setHasFixedSize(true)
        binding.bottomSheet.recyclerViewPhimLe.setItemAnimator(null)
        binding.bottomSheet.recyclerViewPhimLe.post {
            Log.d(
                "hoho",
                "Initial recyclerViewMovie setup: isVisible=${binding.bottomSheet.recyclerViewPhimLe.isVisible}, " +
                        "height=${binding.bottomSheet.recyclerViewPhimLe.height}, itemCount=${filmFullscreenAdapter.itemCount}"
            )
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        historyRepository = WatchHistoryRepository(AppDatabase.invoke(this))
        detailViewModel = MovieViewModel.DetailViewModel(historyRepository)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val filmDetail = viewModel.filmDetail.filterNotNull().first()
                Log.d("hoho", "Film detail received: $filmDetail")
                binding.tvNameFilm.text = filmDetail.name
                val fullText = filmDetail.description
                val shortText =
                    if (fullText.length > 200) fullText.substring(0, 200) + "..." else fullText
                setTextWithReadMore(binding.tvDecription, shortText, fullText)

                binding.tvLuotXem.text = "${filmDetail.viewNumber / 1000}k views"
                binding.tvTitleFullScreen.text = filmDetail.name
                currentMovieTitle = filmDetail.name
                currentMovieImage = filmDetail.avatar ?: ""

                // Get episodes and sort them by episode number
                val episodes = filmDetail.subVideoList ?: emptyList()
                val sortedEpisodes =
                    episodes.sortedBy { it.episode.toString().toIntOrNull() ?: Int.MAX_VALUE }
                Log.d(
                    "hoho",
                    "Episodes before sort: ${episodes.map { "episode=${it.episode}, link=${it.link}" }}"
                )
                Log.d(
                    "hoho",
                    "Episodes after sort: ${sortedEpisodes.map { "episode=${it.episode}, link=${it.link}" }}"
                )

                // Update adapters with sorted list
                episodeAdapter.submitList(sortedEpisodes)
                episodeFullScreenAdapter.submitList(sortedEpisodes)

                if (sortedEpisodes.isNotEmpty()) {
                    binding.bottomSheet.recyclerViewPhimBo.visibility = View.VISIBLE
                    binding.bottomSheet.recyclerViewPhimLe.visibility = View.GONE
                    isPhimBo = true

                    val firstEpisode = sortedEpisodes.firstOrNull()
                    Log.d("hoho", "First episode: $firstEpisode")
                    val history = detailViewModel.getLatestByMovieId(currentFilmId.toString())
                    Log.d("hoho", "History from DB: $history")

                    if (history != null && history.videoLink.isNotEmpty()) {
                        currentVideoLink = history.videoLink
                        currentEpisodeNumber = history.episodeNumber ?: ""
                        playbackPosition = history.lastPosition
                        Log.d(
                            "hoho",
                            "Restored history: link=$currentVideoLink, episode=$currentEpisodeNumber, position=$playbackPosition"
                        )

                        // Find episode index in sorted list
                        val episodeIndex =
                            sortedEpisodes.indexOfFirst { it.episode == currentEpisodeNumber.toInt() }
                        Log.d("hoho", "Episode index for link=$currentVideoLink: $episodeIndex")

                        if (episodeIndex >= 0) {
                            val selectedEpisode = sortedEpisodes[episodeIndex]
                            episodeFullScreenAdapter.setSelectedEpisode(selectedEpisode)
                            episodeAdapter.setSelectedEpisode(selectedEpisode)
                            binding.bottomSheet.recyclerViewPhimBo.scrollToPosition(episodeIndex)
                            adapter.notifyEpisodeSelected(episodeIndex)
                            playEpisode(currentVideoLink, playbackPosition, currentEpisodeNumber)
                        } else {
                            // Fallback to first episode
                            Log.w(
                                "hoho",
                                "Episode not found in sorted list, falling back to first episode"
                            )
                            currentVideoLink = firstEpisode?.link ?: ""
                            currentEpisodeNumber = firstEpisode?.episode.toString() ?: ""
                            firstEpisode?.let { episodeFullScreenAdapter.setSelectedEpisode(it) }
                            firstEpisode?.let { episodeAdapter.setSelectedEpisode(it) }
                            binding.bottomSheet.recyclerViewPhimBo.scrollToPosition(0)
                            adapter.notifyEpisodeSelected(0)
                            playEpisode(currentVideoLink, 0L, currentEpisodeNumber)
                        }
                    } else {
                        // No history, start with first episode
                        Log.d("hoho", "No history found, starting with first episode")
                        currentVideoLink = firstEpisode?.link ?: ""
                        currentEpisodeNumber = firstEpisode?.episode.toString() ?: ""
                        firstEpisode?.let { episodeFullScreenAdapter.setSelectedEpisode(it) }
                        firstEpisode?.let { episodeAdapter.setSelectedEpisode(it) }
                        binding.bottomSheet.recyclerViewPhimBo.scrollToPosition(0)
                        adapter.notifyEpisodeSelected(0)
                        playEpisode(currentVideoLink, 0L, currentEpisodeNumber)
                    }
                } else {
                    binding.bottomSheet.recyclerViewPhimBo.visibility = View.GONE
                    binding.bottomSheet.recyclerViewPhimLe.visibility = View.VISIBLE
                    isPhimBo = false
                    // For non-series movies, use the link from API
                    val videoLink = filmDetail.link ?: ""
                    if (videoLink.isNotEmpty()) {
                        Log.d("hoho", "Playing initial video: $videoLink")
                        playEpisode(videoLink, playbackPosition, null)
                    } else {
                        Log.e("hoho", "No valid video link available")
                        Toast.makeText(
                            this@DetailActivity,
                            "No video available for this film",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Log.d("hoho", "isPhimBo viewmodel: $isPhimBo")
                Log.d("hoho", "Updated episodes: ${episodes.size}")

                filmDetail.categoryId.let { categoryId ->
                    Log.d("hoho", "Fetching related films for categoryId: $categoryId")
                    viewModel.fetchRelatedFilms(categoryId, "")
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.relatedFilms.collectLatest { relatedFilms ->
                    Log.d("hoho", "Related films received: size=${relatedFilms.size}")
                    relatedFilmAdapter.submitList(relatedFilms)
                    filmFullscreenAdapter.submitList(relatedFilms)
                    Log.d(
                        "hoho",
                        "Related films updated: isVisible=${binding.bottomSheet.recyclerViewPhimLe.isVisible}, " +
                                "itemCount=${filmFullscreenAdapter.itemCount}"
                    )
                }
            }
        }
    }

    private fun setupViewPagerAndTabs() {
        adapter = DetailAdapter(supportFragmentManager, lifecycle)
        adapter.setOnMovieClickListener { movie ->
            Log.d("hoho", "Movie selected: ${movie.name}, id: ${movie.id}")
            playEpisode(movie.link ?: "", 0L, null)
        }
        adapter.setOnEpisodeClickListener { episode ->
            Log.d("hoho", "Episode selected: ${episode.episode}, link: ${episode.link}")
            currentEpisodeNumber = episode.episode.toString() ?: ""
            lifecycleScope.launch {
                val episodes = viewModel.filmDetail.value?.subVideoList ?: emptyList()
                val episodeIndex = episodes.indexOfFirst { it.link == episode.link }
                Log.d("hoho", "Selected episode index: $episodeIndex")
                if (episodeIndex >= 0) {
                    episodeFullScreenAdapter.setSelectedEpisode(episodes[episodeIndex])
                    episodeAdapter.setSelectedEpisode(episodes[episodeIndex])
                    binding.bottomSheet.recyclerViewPhimBo.scrollToPosition(episodeIndex)
                    adapter.notifyEpisodeSelected(episodeIndex)
                    val history = detailViewModel.getByMovieIdAndLink(
                        currentFilmId.toString(),
                        episode.link ?: ""
                    )
                    val seekPosition = history?.lastPosition ?: 0L
                    playEpisode(episode.link ?: "", seekPosition, currentEpisodeNumber)
                } else {
                    Log.w("hoho", "Selected episode not found in subVideoList")
                    Toast.makeText(
                        this@DetailActivity,
                        "Episode not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "List of episodes"
                1 -> tab.text = "Suggestions for you"
            }
        }.attach()
    }

    private fun setupAdapters() {
        episodeAdapter = EpisodeListAdapter()
        episodeAdapter.onItemClick = { episode ->
            Log.d("hoho", "=== EpisodeListAdapter Click ===")
            Log.d("hoho", "Selected episode: ${episode.episode}, link: ${episode.link}")
            currentEpisodeNumber = episode.episode.toString() ?: ""
            val sharedEpisodeViewModel =
                ViewModelProvider(this)[MovieViewModel.SharedEpisodeViewModel::class.java]
            sharedEpisodeViewModel.selectEpisode(episode)
            lifecycleScope.launch {
                val episodes = viewModel.filmDetail.value?.subVideoList ?: emptyList()
                Log.d("hoho", "Total episodes: ${episodes.size}")
                Log.d(
                    "hoho",
                    "Episodes list: ${episodes.map { "episode=${it.episode}, link=${it.link}" }}"
                )

                val episodeIndex = episodes.indexOfFirst { it.link == episode.link }
                Log.d("hoho", "EpisodeListAdapter selected episode index: $episodeIndex")
                if (episodeIndex >= 0) {
                    // Update all adapters with the same episode
                    Log.d("hoho", "Updating adapters with episode at index: $episodeIndex")
                    episodeFullScreenAdapter.setSelectedEpisode(episodes[episodeIndex])
                    episodeAdapter.setSelectedEpisode(episodes[episodeIndex])
                    binding.bottomSheet.recyclerViewPhimBo.scrollToPosition(episodeIndex)
                    adapter.notifyEpisodeSelected(episodeIndex)

                    // Get history and play episode
                    val history = detailViewModel.getByMovieIdAndLink(
                        currentFilmId.toString(),
                        episode.link ?: ""
                    )
                    Log.d("hoho", "Found history: $history")
                    val seekPosition = history?.lastPosition ?: 0L
                    Log.d("hoho", "Playing episode with seek position: $seekPosition")
                    playEpisode(episode.link ?: "", seekPosition, currentEpisodeNumber)
                } else {
                    Log.e("hoho", "Episode not found in list! Selected link: ${episode.link}")
                }
            }
        }

        episodeFullScreenAdapter = EpisodeFullScreenAdapter(filmId, avatar)
        episodeFullScreenAdapter.onItemClick = { episode ->
            Log.d("hoho", "=== EpisodeFullScreenAdapter Click ===")
            Log.d("hoho", "Selected episode: ${episode.episode}, link: ${episode.link}")
            currentEpisodeNumber = episode.episode.toString() ?: ""
            val sharedEpisodeViewModel =
                ViewModelProvider(this)[MovieViewModel.SharedEpisodeViewModel::class.java]
            sharedEpisodeViewModel.selectEpisode(episode)
            lifecycleScope.launch {
                val episodes = viewModel.filmDetail.value?.subVideoList ?: emptyList()
                Log.d("hoho", "Total episodes: ${episodes.size}")
                Log.d(
                    "hoho",
                    "Episodes list: ${episodes.map { "episode=${it.episode}, link=${it.link}" }}"
                )

                val episodeIndex = episodes.indexOfFirst { it.link == episode.link }
                Log.d("hoho", "episodeFullScreenAdapter selected episode index: $episodeIndex")
                if (episodeIndex >= 0) {
                    // Update all adapters with the same episode
                    Log.d("hoho", "Updating adapters with episode at index: $episodeIndex")
                    episodeFullScreenAdapter.setSelectedEpisode(episodes[episodeIndex])
                    episodeAdapter.setSelectedEpisode(episodes[episodeIndex])
                    binding.bottomSheet.recyclerViewPhimBo.scrollToPosition(episodeIndex)
                    adapter.notifyEpisodeSelected(episodeIndex)

                    // Get history and play episode
                    val history = detailViewModel.getByMovieIdAndLink(
                        currentFilmId.toString(),
                        episode.link ?: ""
                    )
                    Log.d("hoho", "Found history: $history")
                    val seekPosition = history?.lastPosition ?: 0L
                    Log.d("hoho", "Playing episode with seek position: $seekPosition")
                    playEpisode(episode.link ?: "", seekPosition, currentEpisodeNumber)
                } else {
                    Log.e("hoho", "Episode not found in list! Selected link: ${episode.link}")
                }
            }
        }

        relatedFilmAdapter = RelatedFilmAdapter()
        relatedFilmAdapter.onItemClick = { film ->
            Log.d("hoho", "Related film selected: ${film.name}, id: ${film.id}")
            updateFilm(film.id)
        }

        filmFullscreenAdapter = FilmFullscreenAdapter().apply {
            onItemClick = { film ->
                Log.d("hoho", "Fullscreen film selected: ${film.name}, id: ${film.id}")
                updateFilm(film.id)
            }
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (isFullscreen && e1 != null) {
                    val deltaY = e2.y - e1.y
                    if (deltaY < -100 && abs(velocityY) > 100) {
                        val filmDetail = viewModel.filmDetail.value
                        if (filmDetail != null) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            return true
                        } else {
                            Log.w("hoho", "Swipe up ignored: filmDetail is null")
                            Toast.makeText(
                                this@DetailActivity,
                                "Film data not loaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                return false
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun setupBottomSheet() {
        binding.bottomSheet.apply {
            binding.bottomSheet.recyclerViewPhimBo.layoutManager = LinearLayoutManager(this@DetailActivity)
            binding.bottomSheet.recyclerViewPhimBo.adapter = episodeFullScreenAdapter
            binding.bottomSheet.recyclerViewPhimLe.layoutManager = LinearLayoutManager(this@DetailActivity)
            binding.bottomSheet.recyclerViewPhimLe.adapter = relatedFilmAdapter
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.player_height)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (!isFullscreen) {
                    TransitionManager.beginDelayedTransition(binding.playerContainer)
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            val params = binding.playerContainer.layoutParams
                            params.height = (resources.displayMetrics.heightPixels * 0.3).toInt()
                            binding.playerContainer.layoutParams = params
                            if (isPhimBo) {
                                binding.bottomSheet.recyclerViewPhimLe.isNestedScrollingEnabled = false
                                binding.bottomSheet.recyclerViewPhimBo.isNestedScrollingEnabled = true
                            } else {
                                binding.bottomSheet.recyclerViewPhimLe.isNestedScrollingEnabled = true
                                binding.bottomSheet.recyclerViewPhimBo.isNestedScrollingEnabled = false
                            }
                        }

                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            val params = binding.playerContainer.layoutParams
                            params.height = originalContainerHeight
                            binding.playerContainer.layoutParams = params
                            binding.playerView.showController()
                        }

                        BottomSheetBehavior.STATE_HIDDEN -> {
                            val params = binding.playerContainer.layoutParams
                            params.height = originalContainerHeight
                            binding.playerContainer.layoutParams = params
                            binding.playerView.showController()
                        }

                        BottomSheetBehavior.STATE_DRAGGING -> {
                            binding.playerView.showController()
                        }

                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            val params = binding.playerContainer.layoutParams
                            params.height = (originalContainerHeight * 0.7).toInt()
                            binding.playerContainer.layoutParams = params
                            binding.playerView.showController()
                        }

                        BottomSheetBehavior.STATE_SETTLING -> {
                            binding.playerView.showController()
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (!isFullscreen) {
                    val params = binding.playerContainer.layoutParams
                    val maxHeight = originalContainerHeight
                    val minHeight = (resources.displayMetrics.heightPixels * 0.3).toInt()

                    // Tính toán chiều cao mới dựa trên slideOffset với hiệu ứng easing
                    val easedOffset = if (slideOffset > 0) {
                        // Khi kéo lên, làm chậm lại một chút ở đầu
                        1 - (1 - slideOffset) * (1 - slideOffset)
                    } else {
                        // Khi kéo xuống, làm chậm lại một chút ở cuối
                        slideOffset * slideOffset
                    }

                    val targetHeight = if (slideOffset > 0) {
                        maxHeight - ((maxHeight - minHeight) * easedOffset).toInt()
                    } else {
                        minHeight + ((maxHeight - minHeight) * -easedOffset).toInt()
                    }
                    params.height = targetHeight
                    binding.playerContainer.layoutParams = params
                }
            }
        })

        // Đảm bảo bottom sheet luôn nằm ở dưới cùng và danh sách phim nằm ở dưới
        binding.bottomSheet.root.post {
            val layoutParams = binding.bottomSheet.root.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 0
            binding.bottomSheet.root.layoutParams = layoutParams
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        player?.apply {
            seekTo(currentMediaItemIndex, playbackPosition)
            playWhenReady = this@DetailActivity.playWhenReady
            prepare()
        }

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("hoho", "Player error: ${error.message}", error)
                Toast.makeText(
                    this@DetailActivity,
                    "Error playing video: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> Log.d("hoho", "Player state: IDLE")
                    Player.STATE_BUFFERING -> Log.d("hoho", "Player state: BUFFERING")
                    Player.STATE_READY -> {
                        Log.d("hoho", "Player state: READY")
                        updatePlayPauseButton()
                        binding.tvTotalTime.text = formatTime(player?.duration ?: 0)
                        binding.dtbExoProgress.setDuration(player?.duration ?: 0)
                        binding.dtbExoProgress.setPosition(player?.currentPosition ?: 0)
                        binding.dtbExoProgress.setBufferedPosition(player?.bufferedPosition ?: 0)
                    }

                    Player.STATE_ENDED -> Log.d("hoho", "Player state: ENDED")
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPauseButton()
            }
        })
    }

    private fun anhXa() {
        binding.viewPager.findViewById<ViewPager2>(R.id.view_pager)
        binding.tabLayout.findViewById<TabLayout>(R.id.tab_layout)
        binding.ivBack.findViewById<ImageView>(R.id.iv_back)
        binding.tvNameFilm.findViewById<TextView>(R.id.tv_name_film)
        binding.tvLuotXem.findViewById<TextView>(R.id.tv_luot_xem)
        binding.tvDecription.findViewById<TextView>(R.id.tv_decription)
        binding.playerView.findViewById<PlayerView>(R.id.player_view)
        binding.playerContainer.findViewById<ConstraintLayout>(R.id.player_container)
        binding.llInfo.findViewById<LinearLayout>(R.id.ll_info)
        binding.originalContainerHeight = resources.getDimensionPixelSize(R.dimen.player_height)
        binding.nestedScrollView.findViewById<NestedScrollView>(R.id.nested_scroll_view)
    }

    @OptIn(UnstableApi::class)
    private fun updateFullscreenState() {
        if (isFullscreen) {
            if (isPhimBo) {
                binding.bottomSheet.recyclerViewPhimBo.visibility = View.VISIBLE
                binding.bottomSheet.recyclerViewPhimLe.visibility = View.GONE
            } else {
                binding.bottomSheet.recyclerViewPhimBo.visibility = View.GONE
                binding.bottomSheet.recyclerViewPhimLe.visibility = View.VISIBLE
            }
            binding.bottomSheet.visibility = View.VISIBLE
            binding.tvNameFilm.visibility = View.GONE
            binding.tvLuotXem.visibility = View.GONE
            binding.tvDecription.visibility = View.GONE
            binding.llInfo.visibility = View.GONE
            binding.ivBack.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
            binding.viewPager.visibility = View.GONE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val params = binding.playerContainer.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.playerContainer.layoutParams = params
            binding.llTopControl.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            binding.tvNameFilm.visibility = View.VISIBLE
            binding.tvLuotXem.visibility = View.VISIBLE
            binding.tvDecription.visibility = View.VISIBLE
            binding.llInfo.visibility = View.VISIBLE
            binding.ivBack.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val params = binding.playerContainer.layoutParams
            params.height = originalContainerHeight
            binding.playerContainer.layoutParams = params
            binding.llTopControl.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.bottomSheet.recyclerViewPhimBo.visibility = View.GONE
            binding.bottomSheet.recyclerViewPhimLe.visibility = View.GONE
            binding.bottomSheet.visibility = View.GONE
        }
        binding.playerView.showController()
    }

    @OptIn(UnstableApi::class)
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        updateFullscreenState()
    }

    @OptIn(UnstableApi::class)
    private fun initControllerViews() {
        binding.ibExoPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }

        binding.ibExoRew.setOnClickListener {
            player?.seekTo((player?.currentPosition?.minus(10000) ?: 0).coerceAtLeast(0))
        }

        binding.ibExoFfwd.setOnClickListener {
            player?.seekTo(
                (player?.currentPosition?.plus(10000) ?: 0).coerceAtMost(
                    player?.duration ?: 0
                )
            )
        }

        binding.ibExoFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        binding.ibExoBack.setOnClickListener {
            if (isFullscreen) {
                toggleFullscreen()
            } else {
                finish()
            }
        }

        binding.ivBack.setOnClickListener {
            if (isFullscreen) {
                toggleFullscreen()
            } else {
                finish()
            }
        }
    }

    private fun updatePlayPauseButton() {
        binding.ibExoPlayPause.setImageResource(
            if (player?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    @OptIn(UnstableApi::class)
    private fun playEpisode(videoLink: String, seekPosition: Long, episodeNumber: String?) {
        Log.d("hoho", "=== playEpisode Called ===")
        Log.d("hoho", "Current state before playing:")
        Log.d("hoho", "- Current video link: $currentVideoLink")
        Log.d("hoho", "- Current episode number: $currentEpisodeNumber")
        Log.d("hoho", "- Current film ID: $currentFilmId")

        // Lưu lịch sử của tập hiện tại trước khi chuyển tập
        player?.let { currentPlayer ->
            val currentHistory = HistoryMovie(
                movieId = currentFilmId.toString(),
                videoLink = currentVideoLink,
                movieTitle = currentMovieTitle,
                movieImage = currentMovieImage,
                lastPosition = currentPlayer.currentPosition,
                duration = currentPlayer.duration,
                lastWatched = System.currentTimeMillis(),
                episodeNumber = currentEpisodeNumber
            )
            Log.d("hoho", "Saving current episode history: $currentHistory")
            lifecycleScope.launch {
                detailViewModel.insertOrUpdate(currentHistory)
            }
        }

        currentVideoLink = videoLink
        currentEpisodeNumber = episodeNumber ?: ""
        if (videoLink.isEmpty()) {
            Log.e("hoho", "Empty video link")
            Toast.makeText(this, "Invalid video link", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("hoho", "Playing new episode:")
        Log.d("hoho", "- New video link: $videoLink")
        Log.d("hoho", "- New episode number: $currentEpisodeNumber")
        Log.d("hoho", "- Seek position: $seekPosition")

        val mediaItem = MediaItem.fromUri(videoLink)
        player?.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            playWhenReady = true
            seekTo(seekPosition)
            prepare()
            Log.d("hoho", "Player prepared with media item, seeked to $seekPosition")
        } ?: run {
            Log.e("hoho", "Player is null")
            Toast.makeText(this, "Player initialization failed", Toast.LENGTH_SHORT).show()
        }

        // Lưu lịch sử của tập mới sau khi đã chuẩn bị player
        lifecycleScope.launch {
            val history = HistoryMovie(
                movieId = currentFilmId.toString(),
                videoLink = currentVideoLink,
                movieTitle = currentMovieTitle,
                movieImage = currentMovieImage,
                lastPosition = seekPosition,
                duration = player?.duration ?: 0L,
                lastWatched = System.currentTimeMillis(),
                episodeNumber = currentEpisodeNumber
            )
            Log.d("hoho", "Saving new episode history: $history")
            detailViewModel.insertOrUpdate(history)
        }
    }

    @OptIn(UnstableApi::class)
    private fun initTimeBar() {
        binding.dtbExoProgress.addListener(object : androidx.media3.ui.TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: androidx.media3.ui.TimeBar, position: Long) {
                player?.playWhenReady = false
            }

            override fun onScrubMove(timeBar: androidx.media3.ui.TimeBar, position: Long) {
                binding.tvCurrentTime.text = formatTime(position)
            }

            override fun onScrubStop(
                timeBar: androidx.media3.ui.TimeBar,
                position: Long,
                canceled: Boolean
            ) {
                player?.seekTo(position)
                player?.playWhenReady = true
            }
        })

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    player?.let {
                        if (it.isPlaying || it.playbackState == Player.STATE_READY) {
                            val currentPosition = it.currentPosition
                            val duration = it.duration
                            val bufferedPosition = it.bufferedPosition
                            binding.tvCurrentTime.text = formatTime(currentPosition)
                            binding.tvTotalTime.text = formatTime(duration)
                            binding.dtbExoProgress.setDuration(duration)
                            binding.dtbExoProgress.setPosition(currentPosition)
                            binding.dtbExoProgress.setBufferedPosition(bufferedPosition)
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(timeMs: Long): String {
        if (timeMs <= 0) return "00:00"
        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds - hours * 3600) / 60
        val seconds = (totalSeconds - hours * 3600 - minutes * 60) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun setTextWithReadMore(textView: TextView, shortText: String, fullText: String) {
        val actionText =
            if (isExpanded) " " + getString(R.string.less) else " " + getString(R.string.more)
        val displayText = if (isExpanded) fullText else shortText
        // Remove maxLines constraint when expanded
        if (isExpanded) {
            textView.maxLines = Integer.MAX_VALUE // Allow unlimited lines
        } else {
            textView.maxLines = 5 // Reset to original constraint
        }
        val spannableString = SpannableString(displayText + actionText)

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                isExpanded = !isExpanded
                setTextWithReadMore(textView, shortText, fullText)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.chu_dao)
                ds.isUnderlineText = false
            }
        }, displayText.length, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    @OptIn(UnstableApi::class)
    override fun onPause() {
        super.onPause()
        player?.let {
            playbackPosition = it.currentPosition
            currentMediaItemIndex = it.currentMediaItemIndex
            playWhenReady = it.playWhenReady
            it.pause()
            Log.d("hoho", "Paused at position: $playbackPosition")

            val history = HistoryMovie(
                movieId = currentFilmId.toString(),
                videoLink = currentVideoLink,
                movieTitle = currentMovieTitle,
                movieImage = currentMovieImage,
                lastPosition = playbackPosition,
                duration = it.duration,
                lastWatched = System.currentTimeMillis(),
                episodeNumber = currentEpisodeNumber
            )
            Log.e("hoho", "save watch history: $history")
            detailViewModel.insertOrUpdate(history)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        player?.let {
            it.seekTo(currentMediaItemIndex, playbackPosition)
            it.playWhenReady = playWhenReady
            if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                it.prepare()
                it.play()
            }
            Log.d("hoho", "Resumed at position: $playbackPosition")
        }
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        Log.d("hoho", "Player released")
    }

    private fun updateFilm(filmId: Int) {
        Log.d("hoho", "Updating film with ID: $filmId")

        // Lưu lịch sử của phim hiện tại trước khi chuyển
        player?.let { currentPlayer ->
            val currentHistory = HistoryMovie(
                movieId = currentFilmId.toString(),
                videoLink = currentVideoLink,
                movieTitle = currentMovieTitle,
                movieImage = currentMovieImage,
                lastPosition = currentPlayer.currentPosition,
                duration = currentPlayer.duration,
                lastWatched = System.currentTimeMillis(),
                episodeNumber = currentEpisodeNumber
            )
            Log.d("hoho", "Saving history before switching film: $currentHistory")
            lifecycleScope.launch {
                detailViewModel.insertOrUpdate(currentHistory)
            }
        }

        // Reset trạng thái
        currentFilmId = filmId
        currentVideoLink = ""
        currentEpisodeNumber = ""
        playbackPosition = 0L
        isPhimBo = false
        isFullscreen = false
        currentMovieTitle = ""
        currentMovieImage = ""

        // Reset trình phát
        player?.let {
            it.stop()
            it.clearMediaItems()
            Log.d("hoho", "Player reset for new film")
        } ?: Log.w("hoho", "Player is null during reset")

        // Reset adapters
        episodeAdapter.submitList(emptyList())
        episodeFullScreenAdapter.submitList(emptyList())
        relatedFilmAdapter.submitList(emptyList())
        filmFullscreenAdapter.submitList(emptyList())
        Log.d("hoho", "Adapters reset")

        // Reset giao diện
        binding.tvNameFilm.text = ""
        binding.tvLuotXem.text = ""
        binding.tvDecription.text = ""
        binding.tvTitleFullScreen.text = ""
        binding.bottomSheet.recyclerViewPhimBo.visibility = View.GONE
        binding.bottomSheet.recyclerViewPhimLe.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        updateFullscreenState()
        Log.d("hoho", "UI reset")

        // Tải dữ liệu phim mới
        viewModel.fetchFilmDetail(filmId, "")
        Log.d("hoho", "Fetching film detail for ID: $filmId")
    }
}