package com.client.moviezz.views

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.client.moviezz.adapters.EpisodeListAdapter
import com.client.moviezz.adapters.FilmFullscreenAdapter
import com.client.moviezz.adapters.RelatedFilmAdapter
import com.client.moviezz.models.SubVideo
import com.client.moviezz.viewmodel.MovieViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@UnstableApi
class DetailActivity : AppCompatActivity() {
    private lateinit var topControls: LinearLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: DetailAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewModel: MovieViewModel
    private lateinit var ivBack: ImageView
    private lateinit var ibBack: ImageButton
    private lateinit var tvTitleFullScreen: TextView
    private lateinit var ibLock: ImageButton
    private lateinit var ibRewind: ImageButton
    private lateinit var ibForward: ImageButton
    private lateinit var ibFullScreen: ImageButton
    private lateinit var ibPlay: ImageButton
    private lateinit var ibPrevious: ImageButton
    private lateinit var ibNext: ImageButton
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvNameFilm: TextView
    private lateinit var tvLuotXem: TextView
    private lateinit var tvDecription: TextView
    private lateinit var tvLuotThich: TextView
    private lateinit var playerView: PlayerView
    private lateinit var playerContainer: ConstraintLayout
    private lateinit var llInfo: LinearLayout
    private lateinit var llLuotThich: LinearLayout
    private lateinit var btnShowMore: TextView
    private lateinit var timeBar: DefaultTimeBar
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var recyclerViewEpisode: RecyclerView
    private lateinit var recyclerViewMovie: RecyclerView
    private lateinit var recyclerViewRelatedMovies: RecyclerView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var gestureDetector: GestureDetector
    private lateinit var episodeAdapter: EpisodeListAdapter
    private lateinit var relatedFilmAdapter : RelatedFilmAdapter
    private lateinit var filmFullscreenAdapter: FilmFullscreenAdapter

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var playbackPosition = 0L
    private var currentMediaItemIndex = 0
    private var isFullscreen = false
    private var originalContainerHeight = 0
    private var checkfilmepisode = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let {
            outState.putLong("playback_position", it.currentPosition)
            outState.putInt("current_media_item", it.currentMediaItemIndex)
            outState.putBoolean("play_when_ready", it.playWhenReady)
        }
        outState.putBoolean("is_fullscreen", isFullscreen)
        Log.d("hoho", "Saved state: position=${player?.currentPosition}, fullscreen=$isFullscreen")
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val idfilm = intent.getIntExtra("film_id", 0)
        Log.d("hoho", "Received film_id: $idfilm")
        anhXa()

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playback_position", 0L)
            currentMediaItemIndex = savedInstanceState.getInt("current_media_item", 0)
            playWhenReady = savedInstanceState.getBoolean("play_when_ready", true)
            isFullscreen = savedInstanceState.getBoolean("is_fullscreen", false)
            Log.d("hoho", "Restored state: position=$playbackPosition, fullscreen=$isFullscreen")
        }

        // Khởi tạo BottomSheet
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Khởi tạo GestureDetector
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (isFullscreen && e1 != null && e2 != null) {
                    val deltaY = e2.y - e1.y
                    if (deltaY < -100 && Math.abs(velocityY) > 100) { // Vuốt lên
                        val filmDetail = viewModel.filmDetail.value
                        if (filmDetail != null) {
                            val hasEpisodes = !filmDetail.subVideoList.isNullOrEmpty()
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            recyclerViewEpisode.isVisible = hasEpisodes
                            recyclerViewMovie.isVisible = !hasEpisodes
                            Log.d(
                                "hoho",
                                "Swipe up: hasEpisodes=$hasEpisodes, showing ${if (hasEpisodes) "episodes" else "related movies"}"
                            )
                            // Debug trạng thái recycler_view_related_movies
                            if (!hasEpisodes) {
                                recyclerViewMovie.post {
                                    Log.d(
                                        "hoho",
                                        "recycler_view_related_movies: isVisible=${recyclerViewMovie.isVisible}, height=${recyclerViewMovie.height}, itemCount=${relatedFilmAdapter.itemCount}"
                                    )
                                }
                            }
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

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // Cho phép các sự kiện chạm khác hoạt động
        }

        // Khởi tạo RecyclerView adapters
        episodeAdapter = EpisodeListAdapter()
        episodeAdapter.onItemClick = { episode ->
            checkfilmepisode = true
            playEpisode(episode.link ?: "")
        }
        relatedFilmAdapter = RelatedFilmAdapter()
        filmFullscreenAdapter = FilmFullscreenAdapter().apply {
            onItemClick = { film ->
                checkfilmepisode = false
                Log.d("hoho", "Fullscreen film selected: ${film.name}, id: ${film.id}")
                viewModel.fetchFilmDetail(film.id, "")
            }
        }
        recyclerViewEpisode.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewEpisode.adapter = episodeAdapter
        recyclerViewEpisode.setHasFixedSize(true)
        recyclerViewMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMovie.adapter = filmFullscreenAdapter // Sử dụng relatedFilmAdapter
        recyclerViewMovie.setHasFixedSize(true)
        recyclerViewMovie.isNestedScrollingEnabled =
            true // Bật cuộn cho recycler_view_related_movies

        // Nút đóng BottomSheet
        findViewById<ImageButton>(R.id.ib_close_bottom_sheet).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        player = ExoPlayer.Builder(this).build()
        playerView.player = player
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
                        tvTotalTime.text = formatTime(player?.duration ?: 0)
                        timeBar.setDuration(player?.duration ?: 0)
                        timeBar.setPosition(player?.currentPosition ?: 0)
                        timeBar.setBufferedPosition(player?.bufferedPosition ?: 0)
                    }

                    Player.STATE_ENDED -> Log.d("hoho", "Player state: ENDED")
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPauseButton()
            }
        })

        initControllerViews()
        initTimeBar()

        ivBack.setOnClickListener {
            finish()
        }

        adapter = DetailAdapter(supportFragmentManager, lifecycle)
        adapter.setOnMovieClickListener { movie ->
            checkfilmepisode = false
            Log.d("hoho", "Movie selected: ${movie.name}, id: ${movie.id}")
            playEpisode(movie.link ?: "")
        }
        adapter.setOnEpisodeClickListener { episode ->
            checkfilmepisode = true
            Log.d("hoho", "Episode selected: ${episode.episode}, link: ${episode.link}")
            playEpisode(episode.link ?: "")
        }

        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "List of episodes"
                1 -> tab.text = "Suggestions for you"
            }
        }.attach()

        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        lifecycleScope.launchWhenStarted {
//            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Thu thập filmDetail
                viewModel.filmDetail.collectLatest { filmDetail ->
                    Log.d("hoho", "Film detail: $filmDetail")
                    if (filmDetail != null) {
                        tvNameFilm.text = filmDetail.name
                        tvDecription.text = filmDetail.description
                        tvLuotThich.text = "${filmDetail.star} likes"
                        tvLuotXem.text = "${filmDetail.viewNumber / 1000}k views"
                        tvTitleFullScreen.text = filmDetail.name

                        // Cập nhật danh sách tập phim
                        val episodes = filmDetail.subVideoList ?: emptyList()
                        episodeAdapter.submitList(episodes)
                        Log.d("hoho", "Updated episodes: ${episodes.size}")

                        // Gọi API lấy phim liên quan
                        filmDetail.categoryId.let { categoryId ->
                            Log.d("hoho", "Fetching related films for categoryId: $categoryId")
                            viewModel.fetchRelatedFilms(categoryId, "")
                        }

                        val videoLink =
                            filmDetail.link ?: filmDetail.subVideoList?.firstOrNull()?.link
                        if (videoLink != null && videoLink.isNotEmpty()) {
                            Log.d("hoho", "Playing initial video: $videoLink")
                            playEpisode(videoLink)
                        } else {
                            Log.e("hoho", "No valid video link available")
                            Toast.makeText(
                                this@DetailActivity,
                                "No video available for this film",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e("hoho", "Film detail is null")
                        // Không hiển thị Toast ngay, chờ retry
                    }
//                }
            }
        }

//        lifecycleScope.launchWhenStarted {
////            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                // Thu thập relatedFilms
//                viewModel.relatedFilms.collectLatest { relatedFilms ->
//                    Log.d("hoho", "Related films: size=${relatedFilms.size}")
//                    relatedFilmAdapter.submitList(relatedFilms)
//                    filmFullscreenAdapter.submitList(relatedFilms)
//                    Log.d("hoho", "Updated related films: ${relatedFilms.size}")
//                    viewModel.filmDetail.value?.categoryId?.let { categoryId ->
//                        viewModel.fetchRelatedFilms(categoryId, "")
//                    }
//                    delay(2000)
//                }
////            }
//        }

        // Gọi fetchFilmDetail với retry
        viewModel.fetchFilmDetail(idfilm, "")
        updateFullscreenState()
        setupBottomSheet()
    }

    private fun anhXa() {
        viewPager2 = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        ivBack = findViewById(R.id.iv_back)
        tvNameFilm = findViewById(R.id.tv_name_film)
        tvLuotXem = findViewById(R.id.tv_luot_xem)
        tvDecription = findViewById(R.id.tv_decription)
        tvLuotThich = findViewById(R.id.tv_so_luot_thich)
        playerView = findViewById(R.id.player_view)
        playerContainer = findViewById(R.id.player_container)
        llInfo = findViewById(R.id.ll_info)
        llLuotThich = findViewById(R.id.ll_luot_thich)
        btnShowMore = findViewById(R.id.btn_show_more)
        originalContainerHeight = resources.getDimensionPixelSize(R.dimen.player_height)
        nestedScrollView = findViewById(R.id.nested_scroll_view)
        recyclerViewEpisode = findViewById(R.id.recycler_view_episodes)
        recyclerViewMovie = findViewById(R.id.recycler_view_related_movies)
    }

    @OptIn(UnstableApi::class)
    private fun updateFullscreenState() {
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            ivBack.isVisible = false
//            tvNameFilm.isVisible = false
//            tvLuotXem.isVisible = false
//            tvDecription.isVisible = false
//            tvLuotThich.isVisible = false
//            llInfo.isVisible = false
//            llLuotThich.isVisible = false
//            btnShowMore.isVisible = false
//            viewPager2.isVisible = false
//            tabLayout.isVisible = false
            nestedScrollView.isVisible = false
            val params = playerContainer.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            playerContainer.layoutParams = params
//            ibRewind.visibility = View.VISIBLE
//            ibForward.visibility = View.VISIBLE
            topControls.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            ivBack.isVisible = true
//            tvNameFilm.isVisible = true
//            tvLuotXem.isVisible = true
//            tvDecription.isVisible = true
//            tvLuotThich.isVisible = true
//            llInfo.isVisible = true
//            llLuotThich.isVisible = true
//            btnShowMore.isVisible = true
//            viewPager2.isVisible = true
//            tabLayout.isVisible = true
            nestedScrollView.isVisible = true
            val params = playerContainer.layoutParams
            params.height = originalContainerHeight
            playerContainer.layoutParams = params
//            ibRewind.isVisible = true
//            ibForward.isVisible = true
            topControls.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        playerView.showController()
    }

    @OptIn(UnstableApi::class)
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        updateFullscreenState()
    }

    @OptIn(UnstableApi::class)
    private fun initControllerViews() {
        ibPlay = playerView.findViewById(R.id.ib_exo_play_pause)
        ibFullScreen = playerView.findViewById(R.id.ib_exo_fullscreen)
        ibBack = playerView.findViewById(R.id.ib_exo_back)
        tvTitleFullScreen = playerView.findViewById(R.id.tv_exo_title)
        ibRewind = playerView.findViewById(R.id.ib_exo_rew)
        ibForward = playerView.findViewById(R.id.ib_exo_ffwd)
        ibLock = playerView.findViewById(R.id.ib_exo_lock)
        ibPrevious = playerView.findViewById(R.id.ib_exo_prev)
        ibNext = playerView.findViewById(R.id.ib_exo_next)
        tvCurrentTime = playerView.findViewById(R.id.tv_current_time_position)
        tvTotalTime = playerView.findViewById(R.id.tv_total_time_duration)
        topControls = playerView.findViewById(R.id.ll_top_control)
        timeBar = playerView.findViewById(R.id.dtb_exo_progress)

        ibPlay.setOnClickListener {
            player?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }

        ibRewind.setOnClickListener {
            player?.seekTo((player?.currentPosition?.minus(10000) ?: 0).coerceAtLeast(0))
        }

        ibForward.setOnClickListener {
            player?.seekTo(
                (player?.currentPosition?.plus(10000) ?: 0).coerceAtMost(
                    player?.duration ?: 0
                )
            )
        }

        ibFullScreen.setOnClickListener {
            toggleFullscreen()
        }

        ibBack.setOnClickListener {
            if (isFullscreen) {
                toggleFullscreen()
            } else {
                finish()
            }
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                TransitionManager.beginDelayedTransition(playerContainer)
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        val params = playerContainer.layoutParams
                        params.height = (resources.displayMetrics.heightPixels * 0.5).toInt()
                        playerContainer.layoutParams = params
                        recyclerViewMovie.isNestedScrollingEnabled = true
                        Log.d(
                            "hoho",
                            "BottomSheet expanded: playerContainer height=${params.height}"
                        )
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        val params = playerContainer.layoutParams
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT
                        playerContainer.layoutParams = params
                        playerView.showController()
                        Log.d("hoho", "BottomSheet hidden")
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun updatePlayPauseButton() {
        ibPlay.setImageResource(
            if (player?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    @OptIn(UnstableApi::class)
    private fun playEpisode(videoLink: String) {
        if (videoLink.isEmpty()) {
            Log.e("hoho", "Empty video link")
            Toast.makeText(this, "Invalid video link", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("hoho", "Attempting to play: $videoLink")
        val mediaItem = MediaItem.fromUri(videoLink)
        player?.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            playWhenReady = true
            seekTo(0)
            prepare()
            Log.d("hoho", "Player prepared with media item")
        } ?: run {
            Log.e("hoho", "Player is null")
            Toast.makeText(this, "Player initialization failed", Toast.LENGTH_SHORT).show()
        }
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
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        player?.let {
            it.playWhenReady = playWhenReady
            it.seekTo(playbackPosition)
            if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                it.prepare()
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

    @OptIn(UnstableApi::class)
    private fun initTimeBar() {
        timeBar.addListener(object : androidx.media3.ui.TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: androidx.media3.ui.TimeBar, position: Long) {
                player?.playWhenReady = false
            }

            override fun onScrubMove(timeBar: androidx.media3.ui.TimeBar, position: Long) {
                tvCurrentTime.text = formatTime(position)
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
                            tvCurrentTime.text = formatTime(currentPosition)
                            tvTotalTime.text = formatTime(duration)
                            timeBar.setDuration(duration)
                            timeBar.setPosition(currentPosition)
                            timeBar.setBufferedPosition(bufferedPosition)
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
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}