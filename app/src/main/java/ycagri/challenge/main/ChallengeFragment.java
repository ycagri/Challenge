package ycagri.challenge.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;

import java.util.Calendar;

import ycagri.challenge.interfaces.OnFragmentInteractionListener;

/**
 * Base fragment class for fragments used in the application.
 *
 * @author ycagri
 * @since 11.1.2015.
 */
public class ChallengeFragment extends Fragment {
    private static final String CLIENT_ID = "PWRC42LMLFLMEIPL05NKAQP31TG3I4XDZGPTAYSYJSBGFIGI";
    private static final String CLIENT_SECRET = "FT2E3K22SAYMPRWY0QARIQ0OKKVFOGVLGR1ZFBFPZ2CPVTVH";
    private static final String REQUEST_URL_BASE = "https://api.foursquare.com/v2/venues/";

    protected ProgressBar mProgressBar;

    protected OnFragmentInteractionListener mListener;

    /*protected final Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), R.string.request_error, Toast.LENGTH_LONG).show();
        }
    };*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement OnFragmentInteractionListener!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}