package com.droidbayapps.popularmovies;

/**
 * Created by stephen on 12/19/2015.
 */
public class AverageRatingComparator implements java.util.Comparator<MovieData> {
    @Override
    public int compare(MovieData lhs, MovieData rhs) {
        double leftRating = Double.parseDouble(lhs.getAverageRating());
        double rightRating = Double.parseDouble(rhs.getAverageRating());

        // comparing them backward because Collections.List sorts in ascending order
        return Double.compare(rightRating, leftRating);
    }
}
