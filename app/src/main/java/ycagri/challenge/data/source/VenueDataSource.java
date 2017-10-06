package ycagri.challenge.data.source;

import android.support.annotation.NonNull;

import java.util.List;

import rx.Observable;
import ycagri.challenge.data.Venue;

/**
 * Created by vayen01 on 06/10/2017.
 */

public interface VenueDataSource {

    @NonNull
    Observable<List<Venue>> getVenues();
}