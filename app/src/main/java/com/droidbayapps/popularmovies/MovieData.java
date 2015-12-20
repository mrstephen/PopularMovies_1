package com.droidbayapps.popularmovies;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by stephen on 12/12/2015.
 */
public class MovieData implements Parcelable{
    private String mOriginalTitle;
    private String mMoviePosterImageUri;
    private String mPlotSynopsis;
    private String mAverageRating;
    private String mReleaseDate;
    public final static String PARCELABLE_KEY = MovieData.class.toString();

//    public MovieData(){
//        mOriginalTitle = "";
//        mMoviePosterImageUri = "";
//        mPlotSynopsis = "";
//        mAverageRating = "";
//        mReleaseDate = "";
//    }
    public MovieData(JSONObject jsonObject, Context context){
        try{
            mOriginalTitle = jsonObject.getString( context.getString(R.string.json_key_title) );
            String posterFileName = jsonObject.getString(context.getString(R.string.json_key_poster_image_path));
            mMoviePosterImageUri = context.getString(R.string.themoviedb_poster_base_uri) + posterFileName;
            mPlotSynopsis = jsonObject.getString( context.getString(R.string.json_key_plot_synopsis) );
            mAverageRating = jsonObject.getString( context.getString(R.string.json_key_average_rating) );
            mReleaseDate = jsonObject.getString( context.getString(R.string.json_key_release_date) );
        }
        catch(org.json.JSONException je){
            mOriginalTitle = mMoviePosterImageUri = mPlotSynopsis = mAverageRating = mReleaseDate = null;
        }
    }

    protected MovieData(Parcel in) {
        mOriginalTitle = in.readString();
        mMoviePosterImageUri = in.readString();
        mPlotSynopsis = in.readString();
        mAverageRating = in.readString();
        mReleaseDate = in.readString();
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public String getMoviePosterImageUri(){
        return mMoviePosterImageUri;
    }

    public String getPlotSynopsis(){
        return mPlotSynopsis;
    }

    public String getAverageRating(){
        return mAverageRating;
    }

    public String getReleaseDate(){
        return mReleaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mOriginalTitle);
        dest.writeString(mMoviePosterImageUri);
        dest.writeString(mPlotSynopsis);
        dest.writeString(mAverageRating);
        dest.writeString(mReleaseDate);
    }
}
