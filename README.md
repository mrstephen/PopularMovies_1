# PopularMovies_1
Popular Movies, Part 1

API KEY!!!
Please note that you will have to add your own API key in order for this application to function properly.

To do this, open strings.xml, uncomment out this line:
<!--string name="api_key" translatable="false">INSERT YOUR API KEY HERE AND UNCOMMENT THIS LINE</string-->

And then add your own API key as the value of api_key.

Other notes:

1. I made the minimum SDK version 18 simply because the adjustViewBounds property of ImageView can't expand the image view bounds in version 17 or earlier.

2. For getting the list of "highest rated" movies, I simply took the list of popular movies queried from TheMovieDB and sorted them by rating myself (as opposed to getting a list by rating from TheMovieDB). The reason for this is that the list you get from TheMovieDB, when sorted by rating, is VERY different from the list you get when querying by popularity. Not only this, but some of the movies seem not to have proper (or properly sized) thumbnails, AND some are missing the plot synopsis entirely. However, I did actually code it both ways, so I can change it if it really needs to be this way.
