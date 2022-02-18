package com.example.movieapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.movieapp.models.MovieDetails
import com.example.movieapp.databinding.FragmentMovieDetailBinding
import com.example.movieapp.services.MovieResultService
import com.example.movieapp.services.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetailFragment : Fragment() {

    private lateinit var binding: FragmentMovieDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isLoading(true)
        val id = arguments?.getString("id")
        if (!id.isNullOrEmpty())
            loadMovieDetails(id)
        else {
            Toast.makeText(
                requireContext(),
                "Something went wrong loading movie",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    private fun loadMovieDetails(i: String) {
        val destinationService = ServiceBuilder.buildService(MovieResultService::class.java)
        val requestCall = destinationService.getMovieDetails(i)
        requestCall.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    val movieDetails: MovieDetails? = response.body()
                    setInfoOnScreen(movieDetails)
                } else {
                    Log.e("Response", "Something went wrong ${response.message()}")
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong loading movie",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
                isLoading(false)
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Log.e("Response", "onFailure - Something went wrong $t")
                isLoading(false)
                Toast.makeText(
                    requireContext(),
                    "Something went wrong loading movie",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        })
    }

    fun setInfoOnScreen(movieDetails: MovieDetails?) {
        movieDetails?.let {
            Glide.with(this)
                .load(it.poster)
                .placeholder(R.drawable.ic_noimg)
                .into(binding.ivPoster)

            binding.tvTitle.text = it.title
            binding.tvYear.text = it.year

            binding.tvGenre.text = it.genre
            binding.tvRuntime.text = it.runtime
            binding.tvRating.text = it.imdbRating

            binding.tvSynopsis.text = it.plot

            binding.tvScore.text = it.metascore
            binding.tvReview.text = it.rating.size.toString()
            binding.tvPopularity.text = it.imdbVotes

            binding.tvDirector.text = it.director
            binding.tvWriter.text = it.writer
            binding.tvActors.text = it.actors
        }
    }

    fun isLoading(loading: Boolean) {
        if (loading) {
            binding.flLoading.visibility = View.VISIBLE
            showUI(false)
        } else {
            binding.flLoading.visibility = View.GONE
            showUI(true)
        }
    }

    private fun showUI(show: Boolean) {
        if (show) {
            binding.rlTop.visibility = View.VISIBLE
            binding.llMiddle.visibility = View.VISIBLE
            binding.llBottom.visibility = View.VISIBLE
        } else {
            binding.rlTop.visibility = View.GONE
            binding.llMiddle.visibility = View.GONE
            binding.llBottom.visibility = View.GONE
        }
    }
}