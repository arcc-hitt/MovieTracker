package com.underground.showstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.underground.showstracker.adapters.MovieRecyclerViewAdapter;
import com.underground.showstracker.adapters.NowPlayingAdapter;
import com.underground.showstracker.adapters.OnMovieListener;
import com.underground.showstracker.models.MovieModel;
import com.underground.showstracker.viewmodels.MovieListViewModel;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity implements OnMovieListener {


    RecyclerView moviesCycle;
    private MovieRecyclerViewAdapter adapter;
    ViewPager2 nowPlayingViewPager;
    private NowPlayingAdapter now_playing_adapter;
    TabLayout viewPagerIndicator;
    RelativeLayout loadingLayout;

    //View Model
    private MovieListViewModel movieListViewModel;

    // Search Engine
    // List View object
    ListView listView;

    // Define array adapter for ListView
    ArrayAdapter<String> adapter1;

    // Define array List for List View data
    ArrayList<String> mylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Search Engine
        // initialise ListView with id
        listView = findViewById(R.id.listView);

        // Add items to Array List
        mylist = new ArrayList<>();
        mylist.add("C");
        mylist.add("C++");
        mylist.add("C#");
        mylist.add("Java");
        mylist.add("Advanced java");
        mylist.add("Interview prep with c++");
        mylist.add("Interview prep with java");
        mylist.add("data structures with c");
        mylist.add("data structures with java");

        // Set adapter to ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mylist);
        listView.setAdapter(adapter1);


        //id binding
        moviesCycle = findViewById(R.id.movies_recyclerView);
        nowPlayingViewPager = findViewById(R.id.movies_viewPager);
        viewPagerIndicator = findViewById(R.id.view_pager_indicator);
        loadingLayout  = findViewById(R.id.loading);

        //view-model
        movieListViewModel = new ViewModelProvider(this).get(MovieListViewModel.class);

        //set up UI
        configureRecyclerView();
        configureViewPager();

        //calling the observers
        ObserveMovieTrendingChange();
        ObserveMovieNowPlayingChange();

        //api Calls
        searchMovieApi("", 1);
        searchMovieApiNowPlaying(1);


        movieListViewModel.getLoading().observe(this, loading -> {
            if (loading) {
                loadingLayout.setVisibility(View.VISIBLE);
            } else {
                loadingLayout.setVisibility(View.GONE);
            }
        });

    }

    // Search Engine
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu with items using MenuInflator
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Initialise menu item search bar
        // with id and take its object
        MenuItem searchViewItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        // attach setOnQueryTextListener
        // to search view defined above
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Override onQueryTextSubmit method which is call when submit query is searched
            @Override
            public boolean onQueryTextSubmit(String query) {
                // If the list contains the search query than filter the adapter
                // using the filter method with the query as its argument
                if (mylist.contains(query)) {
                    adapter.getFilter().filter(query);
                } else {
                    // Search query not found in List View
                    Toast.makeText(MovieListActivity.this, "Not found", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            // This method is overridden to filter the adapter according
            // to a search query when the user is typing search
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //observing changes
    private void ObserveMovieTrendingChange() {
        movieListViewModel.getMovies().observe(this, new Observer<List<MovieModel>>() {
            @Override
            public void onChanged(List<MovieModel> movieModels) {
                // observe any data change
                if (movieModels != null) {
                    for (MovieModel movieModel : movieModels) {
                        //get the data in log

                        adapter.setmMovies(movieModels);
                    }
                }
            }
        });
    }

    private void ObserveMovieNowPlayingChange() {
        movieListViewModel.getNowPlaying().observe(this, new Observer<List<MovieModel>>() {
            @Override
            public void onChanged(List<MovieModel> movieModels) {
                // observe any data change
                if (movieModels != null) {
                    for (MovieModel movieModel : movieModels) {
                        now_playing_adapter.setNowPlayingMovies(movieModels);
                    }
                }
            }
        });
    }

    //calling the trending in main-activity
    private void searchMovieApi(String query, int pageNumber) {
        movieListViewModel.searchMovieApi(query, pageNumber);
    }

    //calling the now-playing in main activity
    private void searchMovieApiNowPlaying(int pageNumber) {
        movieListViewModel.searchMovieApiNowPlaying(pageNumber);
    }

    private void configureRecyclerView() {
        adapter = new MovieRecyclerViewAdapter(this);
        moviesCycle.setAdapter(adapter);
        moviesCycle.setLayoutManager(new LinearLayoutManager(this));

        //Pagination support
        moviesCycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!moviesCycle.canScrollVertically(1)) {
                    //here we need to get another page of data
                    movieListViewModel.searchNextPage();
                }
            }
        });

    }

    private void configureViewPager() {
        now_playing_adapter = new NowPlayingAdapter(this);
        nowPlayingViewPager.setAdapter(now_playing_adapter);
        //indicator
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(viewPagerIndicator, nowPlayingViewPager, true, true, (tab, position) -> {
            //something to do that i am not sure of
        });
        tabLayoutMediator.attach();

    }
    @Override
    public void onMovieClick(int position) {
        //sending data to detail intent
        Intent detailIntent = new Intent(MovieListActivity.this,MovieDetailsActivity.class);
        detailIntent.putExtra("movie", adapter.getSelectedMovie(position));
        startActivity(detailIntent);

    }
    @Override
    public void onMovieClickNowPlaying(int position) {
        Intent detailIntent = new Intent(MovieListActivity.this, MovieDetailsActivity.class);
        detailIntent.putExtra("movie", now_playing_adapter.getSelectedMovie(position));
        startActivity(detailIntent);
    }

    /* private void getRetrofitResponse() {
        MovieApi movieApi = Servicey.getMovieApi();

        Call<MovieSearchResponse> responseCall = movieApi
                .searchMovie(
                        Credentials.API_KEY,
                        "Action",
                        2);

        responseCall.enqueue(new Callback<MovieSearchResponse>() {
            @Override
            public void onResponse(Call<MovieSearchResponse> call, Response<MovieSearchResponse> response) {
                if (response.code() == 200) {
                    //Toast.makeText(MovieListActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();

                    List<MovieModel> movies = new ArrayList<>(response.body().getMovies());
                    for (MovieModel movie : movies) {
                        Toast.makeText(MovieListActivity.this, movie.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Toast.makeText(MovieListActivity.this, "error " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<MovieSearchResponse> call, Throwable t) {

            }
        });
    }*/
}