package com.droidbayapps.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridActivityFragment extends Fragment {

    private GridView mGridView = null;
    private MovieListAdapter mAdapter = null;
    //private MovieData [] mMovieData = null;
    private ArrayList<MovieData> mMovieList = null;

    public MovieGridActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_grid_fragment, menu);

        MenuItem item = menu.findItem(R.id.sortSpinner);
        final Spinner s = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sort_method_array, R.layout.sort_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(R.layout.sort_spinner_dropdown_item);
        s.setAdapter(spinnerAdapter);

        // Had to post the following code as a runnable for this reason: When the spinner layout is created, it
        // automatically sets the selected item to 0. I researched a solution, but it seems that there
        // is no way to prevent this. And if that happens after the OnItemSelectedListener
        // has been set, then it will overwrite the user-selected sort option force it
        // to "Most popular".
        // Posting this as a runnable allows the OnItemSelectedListener to be set after this occurs.
        s.post(new Runnable() {
            @Override
            public void run() {
                // Load the sort preference and set the appropriate value in the spinner
                SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
                String sortPreference = preferences.getString(getString(R.string.sort_preference_key), getString(R.string.sort_popularity_desc));

                String[] values = getResources().getStringArray(R.array.sort_query_values);

                // Find the corresponding item in the spinner and select it
                if(values != null){
                    int sortMethodIndex = -1;
                    for(int idx = 0; sortMethodIndex < 0 && idx < values.length; idx++){
                        if(values[idx].equals(sortPreference)){
                            sortMethodIndex = idx;
                        }
                    }

                    if(sortMethodIndex >= 0){
                        s.setSelection(sortMethodIndex, false);
                    }
                }

                // Set the OnItemSelectedListener to respond to user selection
                s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String[] values = getResources().getStringArray(R.array.sort_query_values);

                        // Save the new sort option in the SharedPreferences
                        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
                        preferences.edit().putString(getString(R.string.sort_preference_key), values[position]).apply();

                        // Reload the movie list
                        refreshMovieList();

                        mGridView.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        mGridView = (GridView)rootView.findViewById(R.id.movieThumbnailGridView);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (/*mMovieData*/mMovieList != null && /*mMovieData.length*/mMovieList.size() > position) {
                    if (mMovieList.get(position).getOriginalTitle().isEmpty()) {
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.no_movie_error_msg), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable(MovieData.PARCELABLE_KEY, mMovieList.get(position) /*mMovieData[position]*/);
                        intent.putExtras(b);
                        getActivity().startActivity(intent);
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshMovieList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void refreshMovieList() {
        if( ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            // NOTE TO REVIEWER: if a separate API request is needed for getting a list of highest rated movies, then
            // I will uncomment the following two lines, and sortPreferences will be passed into
            // task.execute()
//            SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
//            String sortPreference = preferences.getString(getString(R.string.sort_preference_key), getString(R.string.sort_popularity_desc));

            FetchMovieDataTask task = new FetchMovieDataTask();
            task.execute(/*sortPreference*/getString(R.string.sort_popularity_desc));

        }
        else{
            Toast.makeText(getActivity(), getString(R.string.notifyNoInternetPermission), Toast.LENGTH_LONG).show();
        }
    }

    private class FetchMovieDataTask extends AsyncTask<String, Void, MovieData[]>{

        @Override
        protected MovieData[] doInBackground(String... params) {

            MovieData [] movieData;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonResponseString = null;

            try{
                Uri discoveryUri = Uri.parse(getString(R.string.themoviedb_discovery_base_uri)).buildUpon()
                        .appendQueryParameter(getString(R.string.sort_by_query_param), params[0])
                        .appendQueryParameter(getString(R.string.api_key_query_param), getString(R.string.api_key))
                        .build();

                URL url = new URL(discoveryUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonResponseString = buffer.toString();

                movieData = getMovieDataFromJSONString(jsonResponseString);
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieGridFragment", "Error closing stream", e);
                    }
                }
            }

            return movieData;
        }

        private MovieData[] getMovieDataFromJSONString(String jsonResponseString) {

            MovieData[] movieData = null;

            try {
                JSONObject jsonObject = new JSONObject(jsonResponseString);
                JSONArray jsonMovieArray = jsonObject.getJSONArray(getString(R.string.json_key_results));
                if(jsonMovieArray != null){
                    movieData = new MovieData[jsonMovieArray.length()];

                    for(int idx = 0; idx < jsonMovieArray.length(); idx++){
                        movieData[idx] = new MovieData(jsonMovieArray.getJSONObject(idx), getActivity());
                    }
                }
            }
            catch(org.json.JSONException je){

            }

            return movieData;
        }

        @Override
        protected void onPostExecute(MovieData [] movieData) {
            super.onPostExecute(movieData);

            // Handle the case where I didn't get any data back from the query (no connection?)
            if(null == movieData){
                //Just fill with empty data. The image views will show a loading error to the user
                mMovieList = new ArrayList<>();
                mAdapter = new MovieListAdapter(getActivity(), mMovieList);
                mGridView.setAdapter(mAdapter);
                Toast toast = Toast.makeText(getActivity(), getString(R.string.no_movie_error_msg), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            ArrayList<MovieData> sortedMovies = SortMovieData(movieData);
            if(mGridView != null) {
                if(mMovieList == null) {
                    mMovieList = sortedMovies;
                    mAdapter = new MovieListAdapter(getActivity(), mMovieList);
                    mGridView.setAdapter(mAdapter);
                }
                else {
                    mMovieList.clear();
                    mMovieList.addAll(sortedMovies);
                    mAdapter.notifyDataSetChanged();
                }
            }

        }

        private ArrayList<MovieData> SortMovieData(MovieData[] movieData) {
            ArrayList<MovieData> sortedMovies = new ArrayList<MovieData>(Arrays.asList(movieData));

            SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
            String sortPreference = preferences.getString((getString(R.string.sort_preference_key)), getString(R.string.sort_popularity_desc));

            // The list is sorted by popularity by default, so if "by popularity" is the choice, do nothing
            if(sortPreference.compareTo(getString(R.string.sort_popularity_desc)) == 0) {
                // Do nothing
            }
            else{
                Collections.sort(sortedMovies, new AverageRatingComparator());
            }

            return sortedMovies;
        }
    }
}
