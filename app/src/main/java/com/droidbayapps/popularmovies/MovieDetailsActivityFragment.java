package com.droidbayapps.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {

    MovieData mMovieData = null;
    View mRootView = null;
    private final String LOG_TAG = MovieDetailsActivityFragment.class.toString();

    public MovieDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        if(null == savedInstanceState) {
            Bundle extras = getActivity().getIntent().getExtras();
            mMovieData = (MovieData)extras.getParcelable(MovieData.PARCELABLE_KEY);
            if(mMovieData != null){
                TextView titleTextView = (TextView)mRootView.findViewById(R.id.movieTitleTextView);
                if(titleTextView != null)
                    titleTextView.setText(mMovieData.getOriginalTitle());
            }
        }
        else{
            mMovieData = (MovieData)savedInstanceState.getParcelable(MovieData.PARCELABLE_KEY);
        }

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(MovieData.PARCELABLE_KEY, mMovieData);
    }



    @Override
    public void onStart() {
        super.onStart();

        loadMovieDetails();
    }

    private void loadMovieDetails() {
        if(mMovieData != null && mRootView != null){

            try {
                ImageView imageView = (ImageView) mRootView.findViewById(R.id.moviePoster);
                Picasso.with(getActivity()).load(mMovieData.getMoviePosterImageUri()).into(imageView);

                TextView textView = (TextView) mRootView.findViewById(R.id.movieTitleTextView);
                textView.setText(mMovieData.getOriginalTitle());

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_string));
                calendar.setTime(sdf.parse(mMovieData.getReleaseDate()));

                textView = (TextView) mRootView.findViewById(R.id.releaseDateTextView);
                textView.setText(String.format("%d", calendar.get(Calendar.YEAR)));

                textView = (TextView) mRootView.findViewById(R.id.ratingTextView);
                double d = Double.parseDouble(mMovieData.getAverageRating());
                textView.setText(String.format(getString(R.string.average_rating_format_string), d));

                textView = (TextView) mRootView.findViewById(R.id.plotSynopsisTextView);
                textView.setText(mMovieData.getPlotSynopsis());
            }
            catch(java.text.ParseException pe){
                Log.e(LOG_TAG, pe.toString());
            }
        }
    }
}
