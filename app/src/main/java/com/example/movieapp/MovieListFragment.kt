package com.example.movieapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.models.MovieResult
import com.example.movieapp.models.MovieSearch
import com.example.movieapp.databinding.FragmentMovieListBinding
import com.example.movieapp.services.MovieResultService
import com.example.movieapp.services.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieListFragment : Fragment() {
    lateinit var adapter: RecyclerViewAdapter
    private var isScrollLoading: Boolean = false
    lateinit var gridlayoutManager: GridLayoutManager
    private var visibleThreshold = 4
    private var lastVisibleItem = 0
    private var totalItemCount = 0
    private val numOfGridColumn = 2
    private var page = 1

    lateinit var listOfMovie: ArrayList<MovieSearch?>
    lateinit var loadMoreListOfMovie: ArrayList<MovieSearch?>

    private lateinit var binding: FragmentMovieListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (this::listOfMovie.isInitialized && listOfMovie.size > 0) {
            isMovieListEmpty(false)
            setUpRecyclerViews()
        }

        gridlayoutManager = GridLayoutManager(requireContext(), numOfGridColumn)
        binding.rvMovieList.layoutManager = gridlayoutManager

        binding.etMovieSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchMovie(binding.etMovieSearch)
            }
            true
        }

        binding.etMovieSearchInputLayout.setEndIconOnClickListener {
            binding.etMovieSearch.text?.clear()
            isMovieListEmpty(true)
            resetList()
        }
    }

    private fun searchMovie(view: View) {
        resetList()
        val searchedTitle = binding.etMovieSearch.text.toString()
        if (searchedTitle.isNotEmpty()) {
            showMainLoading(true)
            retrieveMovieFromAPI(false, searchedTitle, page)
        }
        hideSoftKeyboard(view)
    }

    private fun addScrollListener() {
        binding.rvMovieList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //total no. of items
                totalItemCount = gridlayoutManager.itemCount
                //last visible item position
                lastVisibleItem = gridlayoutManager.findLastCompletelyVisibleItemPosition()
                if (!isScrollLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    loadMore()
                    isScrollLoading = true
                }
            }
        })
    }

    internal fun loadMore() {
        //Add Scroll loading view
        Handler(Looper.getMainLooper()).post {
            listOfMovie.add(null)
            adapter.notifyItemInserted(listOfMovie.size - 1)
        }

        binding.rvMovieList.postDelayed({
            //Remove Scroll loading view in list.
            listOfMovie.removeAt(listOfMovie.size - 1)
            val listSize = listOfMovie.size
            adapter.notifyItemRemoved(listSize)

            retrieveMovieFromAPI(true, binding.etMovieSearch.text.toString(), ++page)

        }, 1000)
    }

    fun setUpRecyclerViews() {
        adapter = RecyclerViewAdapter(requireContext(), listOfMovie)
        binding.rvMovieList.adapter = adapter
        addScrollListener()

        adapter.onItemClick = { id ->
            val bundle = bundleOf("id" to id)
            findNavController().navigate(
                R.id.action_MovieListFragment_to_MovieDetailFragment,
                bundle
            )
        }

        gridlayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(p0: Int): Int {
                return when (adapter.getItemViewType(p0)) {
                    RecyclerViewAdapter.VIEW_TYPE_PROGRESS -> numOfGridColumn
                    RecyclerViewAdapter.VIEW_TYPE_DATA -> 1
                    else -> -1
                }
            }
        }
    }

    private fun retrieveMovieFromAPI(isLoadMore: Boolean, searchedTitle: String, page: Int) {
        val destinationService = ServiceBuilder.buildService(MovieResultService::class.java)
        val requestCall = destinationService.getMovieList(searchedTitle, page)
        requestCall.enqueue(object : Callback<MovieResult> {
            override fun onResponse(call: Call<MovieResult>, response: Response<MovieResult>) {
                if (response.isSuccessful) {
                    val movieResult = response.body()
                    if (movieResult?.response == true) {
                        if (!isLoadMore) {
                            listOfMovie = movieResult.search as ArrayList<MovieSearch?>
                            setUpRecyclerViews()
                            isMovieListEmpty(false)
                        } else {
                            loadMoreListOfMovie = movieResult.search as ArrayList<MovieSearch?>
                            listOfMovie.addAll(loadMoreListOfMovie)
                            adapter.notifyDataSetChanged()
                            isScrollLoading = false
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No movies found",
                            Toast.LENGTH_SHORT
                        ).show()
                        isScrollLoading = false
                    }
                } else {
                    Log.d("Response", "Something went wrong ${response.message()}")
                    isMovieListEmpty(true)
                }

                showMainLoading(false)
            }

            override fun onFailure(call: Call<MovieResult>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong retrieving movie list",
                    Toast.LENGTH_SHORT
                ).show()
                isMovieListEmpty(true)
                showMainLoading(false)
            }
        })
    }

    fun showMainLoading(loading: Boolean) {
        if (loading)
            binding.flLoading.visibility = View.VISIBLE
        else
            binding.flLoading.visibility = View.GONE
    }

    fun isMovieListEmpty(empty: Boolean) {
        if (empty) {
            binding.flNoSearchedItem.visibility = View.VISIBLE
            binding.rvMovieList.visibility = View.GONE
        } else {
            binding.flNoSearchedItem.visibility = View.GONE
            binding.rvMovieList.visibility = View.VISIBLE
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun resetList() {
        page = 1
        if (this::listOfMovie.isInitialized && this::adapter.isInitialized) {
            listOfMovie.clear()
            adapter.notifyDataSetChanged()
        }
    }
}