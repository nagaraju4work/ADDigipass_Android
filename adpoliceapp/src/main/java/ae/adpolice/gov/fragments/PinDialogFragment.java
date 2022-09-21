package ae.adpolice.gov.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.chaos.view.PinView;
import com.davidmiguel.numberkeyboard.NumberKeyboard;
import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.vasco.digipass.sdk.DigipassSDK;
import com.vasco.digipass.sdk.DigipassSDKReturnCodes;
import com.vasco.digipass.sdk.responses.ValidationResponse;

import java.util.Locale;
import java.util.Objects;

import ae.adpolice.gov.Constants;
import ae.adpolice.gov.R;
import ae.adpolice.gov.users.UserSession;
import ae.adpolice.gov.utils.Utils;

public class PinDialogFragment extends DialogFragment {

    private PinView mPinView;
    private String description;

    public PinDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public interface OnPinSetListener {
        void onPinSet(String pin);

        void onPinNotMatched();

        void onPinSetFailed();
    }

    private boolean toValidate = false;

    private boolean isToValidate() {
        return toValidate;
    }

    public void setToValidate(boolean toValidate) {
        this.toValidate = toValidate;
    }

    private OnPinSetListener onPinSetListener;

    private OnPinSetListener getOnPinSetListener() {
        return onPinSetListener;
    }

    public void setOnPinSetListener(OnPinSetListener onPinSetListener) {
        this.onPinSetListener = onPinSetListener;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_pin_dialog_fragment, container);
        mPinView = view.findViewById(R.id.firstPinView);
        NumberKeyboard numberKeyboard = view.findViewById(R.id.numberKeyboard);
        numberKeyboard.setListener(new NumberKeyboardListener() {
            @Override
            public void onNumberClicked(int number) {
                mPinView.setText(String.format(Locale.getDefault(),"%s%d", Objects.requireNonNull(mPinView.getText()).toString(), number));
            }

            @Override
            public void onLeftAuxButtonClicked() {
            }

            @Override
            public void onRightAuxButtonClicked() {
                if(Objects.requireNonNull(mPinView.getText()).length()>0) {
                    mPinView.setText(String.format(Locale.getDefault(), "%s", Objects.requireNonNull(mPinView.getText()).subSequence(0, mPinView.getText().length() - 1).toString()));
                }
            }
        });
        if (description != null)
            ((TextView) view.findViewById(R.id.tvEnterPinMsg)).setText(description);
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (onPinSetListener != null)
                    getOnPinSetListener().onPinSetFailed();
            }
        });
        mPinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0)
                    ((TextView) view.findViewById(R.id.tvErrorMessage)).setText("");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //not required
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 6) {
                    if (isToValidate()) {
                        ValidationResponse validationResponse = DigipassSDK.validatePasswordWithFingerprint(
                                UserSession.getInstance(getActivity()).getStaticVector(UserSession.getInstance(getActivity()).getCurrentUser().getUserId()),
                                UserSession.getInstance(getActivity()).getDynamicVectorPin(UserSession.getInstance(getActivity()).getCurrentUser().getUserId()),
                                s.toString(), Constants.getDevicePlatformFingerprintForDigipass(getActivity()));
                        UserSession.getInstance(getActivity()).setDynamicVectorPin(UserSession.getInstance(getActivity()).getCurrentUser().getUserId(), validationResponse.getDynamicVector());
                        if (validationResponse.getReturnCode() == DigipassSDKReturnCodes.SUCCESS) {
                            if (onPinSetListener != null) {
                                dismiss();
                                getOnPinSetListener().onPinSet(s.toString());
                            }
                        } else {
                            if (validationResponse.getAttemptLeft() == 0) {
                                dismiss();
                                getOnPinSetListener().onPinSetFailed();
                            }
                            Toast.makeText(getActivity(), "Authentication Failed. " + DigipassSDK.getMessageForReturnCode(validationResponse.getReturnCode()), Toast.LENGTH_SHORT).show();
                            Utils.Log("The password generation has FAILED ", "[  generateResponse.getReturnCode() : "
                                    + DigipassSDK.getMessageForReturnCode(validationResponse.getReturnCode()) + " ]");
                            ((TextView) view.findViewById(R.id.tvEnterPinMsg)).setText(getString(R.string.enter_your_pin));
                            if (description != null)
                                ((TextView) view.findViewById(R.id.tvEnterPinMsg)).setText(description);
                            ((TextView) view.findViewById(R.id.tvErrorMessage)).setText(getString(R.string.pin_not_match, validationResponse.getAttemptLeft()));
                            mPinView.setText("");
                        }
                        return;
                    }
                    if (!Utils.getInstance().containsKey(Constants.PIN_KEY)) {
                        Utils.getInstance().putStringInSecureCache(Constants.PIN_KEY,s.toString());
                        mPinView.setText("");
                        ((TextView) view.findViewById(R.id.tvEnterPinMsg)).setText(getString(R.string.orch_pin_title_confirm));
                        return;
                    }
                    if (Utils.getInstance().getStringFromSecureCache(Constants.PIN_KEY).equals(s.toString())) {
                        if (!DigipassSDK.isPasswordWeak(Utils.getInstance().getStringFromSecureCache(Constants.PIN_KEY))) {
                            dismiss();
                            if (onPinSetListener != null)
                                getOnPinSetListener().onPinSet(Utils.getInstance().getStringFromSecureCache(Constants.PIN_KEY));
                        } else {
                            ((TextView) view.findViewById(R.id.tvErrorMessage)).setText(getString(R.string.orch_pin_error_pin_weak));
                        }
                        Utils.getInstance().removeKey(Constants.PIN_KEY);
                        mPinView.setText("");
                    } else {
                        Utils.getInstance().removeKey(Constants.PIN_KEY);
                        mPinView.setText("");
                        ((TextView) view.findViewById(R.id.tvEnterPinMsg)).setText(getString(R.string.orch_pin_title));
                        ((TextView) view.findViewById(R.id.tvErrorMessage)).setText(getString(R.string.orch_pin_error_no_match));
                        if (onPinSetListener != null)
                            getOnPinSetListener().onPinNotMatched();
                    }
                }
            }
        });
        Objects.requireNonNull(getDialog()).setTitle("PIN");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
    }
}
