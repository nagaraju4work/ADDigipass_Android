package ae.adpolice.gov;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.vasco.digipass.sdk.DigipassSDK;
import com.vasco.digipass.sdk.responses.DigipassPropertiesResponse;

import ae.adpolice.gov.users.UserSession;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetInfoFragment extends DialogFragment {


    public GetInfoFragment() {
        // Required empty public constructor
    }

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_get_info, container, false);
        getUserInformation();
        return view;
    }

    private void getUserInformation() {
        final byte[] dynamicVector =
                UserSession.getInstance(getActivity()).getDynamicVector();
        // Get user information
//        UserInformation userInfo = ((MainActivity) getActivity()).getOrchestratorUserInfo();
        ((TextView) findViewById(R.id.text_get_info_userIdentifier)).setText(UserSession.getInstance(getActivity()).getCurrentUser().getUserId());
        DigipassPropertiesResponse digipassPropertiesResponse = DigipassSDK.getDigipassProperties(UserSession.getInstance(getActivity()).getStaticVector(UserSession.getInstance(getActivity()).getCurrentUser().getUserId()), dynamicVector);
        ((TextView) findViewById(R.id.text_get_info_digipassSerialNumber)).setText(digipassPropertiesResponse.getSerialNumber());
        ((TextView) findViewById(R.id.text_get_info_digipassSequenceNumberUnprotected)).setText(String.valueOf(digipassPropertiesResponse.getSequenceNumber()));
//        ((TextView) findViewById(R.id.text_get_info_digipassSequenceNumberProtected)).setText(String.valueOf(userInfo.getDigipassSequenceNumberProtected()));
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        ((TextView) findViewById(R.id.text_get_info_clientServerTimeShiftSeconds)).setText(String.valueOf(UserSession.getInstance(getActivity()).getClientServerTimeShift()));
    }

    private View findViewById(int text_get_info_userIdentifier) {
        return view.findViewById(text_get_info_userIdentifier);
    }
}
