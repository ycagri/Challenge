package ycagri.challenge.data.source;

import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ycagri.challenge.data.Venue;
import ycagri.challenge.data.VenueLocation;
import ycagri.challenge.data.VenuePhoto;
import ycagri.challenge.data.source.local.LocalDataSource;
import ycagri.challenge.data.source.remote.RemoteDataSource;
import ycagri.challenge.util.SharedPreferenceManager;

/**
 * Created by vayen01 on 06/10/2017.
 */

public class VenueRepository implements VenueDataSource {

    private static final String TAG = "Repository";

    private static final int REFRESH_TIME_DIFFERENCE = 60 * 60 * 1000;

    private static final int REFRESH_DISTANCE_DIFFERENCE = 5;

    private final RemoteDataSource mVenueRemoteDataSource;

    private final LocalDataSource mVenueLocalDataSource;

    private final SharedPreferenceManager mSharedPreferencesManager;

    /**
     * By marking the constructor with {@code @Inject}, Dagger will try to inject the dependencies
     * required to create an instance of the TasksRepository. Because {@link VenueDataSource} is an
     * interface, we must provide to Dagger a way to build those arguments, this is done in
     * {@link VenueRepositoryModule}.
     * <p>
     * When two arguments or more have the same type, we must provide to Dagger a way to
     * differentiate them. This is done using a qualifier.
     * <p>
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    VenueRepository(@Remote RemoteDataSource venueRemoteDataSource,
                    @Local LocalDataSource venueLocalDataSource,
                    SharedPreferenceManager sharedPreferenceManager) {
        mVenueRemoteDataSource = venueRemoteDataSource;
        mVenueLocalDataSource = venueLocalDataSource;
        mSharedPreferencesManager = sharedPreferenceManager;
    }

    /**
     * Gets venues from  local data source (SQLite).
     */
    @Override
    public Observable<List<Venue>> getVenues(double latitude, double longitude) {
        if (forceRefresh(latitude, longitude)) {
            Log.d(TAG, "Retrieving from remote...");
            mSharedPreferencesManager.putLatestLatitude(latitude);
            mSharedPreferencesManager.putLatestLongitude(longitude);
            mSharedPreferencesManager.putLatestUpdateTime(System.currentTimeMillis());
            mVenueRemoteDataSource.getVenues(latitude, longitude)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(mVenueLocalDataSource::insertVenues);
        }

        return mVenueLocalDataSource.getVenues();
    }

    @Override
    public Observable<List<VenuePhoto>> getVenuePhotos(String venueId) {
        mVenueLocalDataSource.getPhotoCount(venueId)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(count -> {
                    if (count == 0) {
                        Log.d(TAG, "Retrieving photos from remote...");
                        mVenueRemoteDataSource.getVenuePhotos(venueId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.computation())
                                .subscribe(venuePhotos -> mVenueLocalDataSource.insertVenuePhotos(venuePhotos, venueId));
                    }
                });

        return mVenueLocalDataSource.getVenuePhotos(venueId);
    }

    @Override
    public Observable<VenueLocation> getVenueLocation(String venueId) {
        return mVenueLocalDataSource.getVenueLocation(venueId);
    }

    private boolean forceRefresh(double latitude, double longitude) {
        long lastTime = mSharedPreferencesManager.getLatestUpdateTime();
        double lastLatitude = mSharedPreferencesManager.getLatestLatitude();
        double lastLongitude = mSharedPreferencesManager.getLatestLongitude();

        double distance = getDistance(lastLatitude, lastLongitude, latitude, longitude);

        return System.currentTimeMillis() - lastTime > REFRESH_TIME_DIFFERENCE || distance > REFRESH_DISTANCE_DIFFERENCE;
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }
}
