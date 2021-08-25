package com.fosents.canyoucount;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private ImageView imageViewBackground, imageViewCloseSettings;
    private RadioGroup radioGroupDifficulty;
    private RadioButton radioButtonEasy, radioButtonMedium, radioButtonHard;
    private ActionListener mListener;
    private String currentDifficulty;
    private EditText editTextUsername;
    private SharedPreferences prefs;
    private String username;
    private TextView textViewReset;

//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

//    private String mParam1;
//    private String mParam2;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener = (ActionListener) getActivity();
        handleBackButton();
        imageViewBackground = view.findViewById(R.id.imageViewSettingsBackground);
        radioButtonEasy = view.findViewById(R.id.radioButtonEasy);
        radioButtonMedium = view.findViewById(R.id.radioButtonMedium);
        radioButtonHard = view.findViewById(R.id.radioButtonHard);
        imageViewCloseSettings = view.findViewById(R.id.imageViewCloseSettings);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        textViewReset = view.findViewById(R.id.textViewReset);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        imageViewBackground.setOnClickListener(v -> {});
        username = prefs.getString(getString(R.string.preferences_username), getString(R.string.preferences_username_default));
        editTextUsername.setText(username);
        editTextUsername.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                playSound(R.raw.click_settings);
                editTextUsername.clearFocus();
            }
            return false;
        });
        editTextUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String usernameNew = editTextUsername.getText().toString();
                if (!usernameNew.equals("")) {
                    username = usernameNew;
                    prefs.edit().putString(getString(R.string.preferences_username), username).apply();
                    mListener.onChangeUsername(username);
                } else editTextUsername.setText(username);
            }
        });
        currentDifficulty = prefs.getString(getString(R.string.preferences_difficulty), getString(R.string.settings_radio_easy));
        if (currentDifficulty.equals(getString(R.string.settings_radio_hard))) {
            radioButtonHard.setChecked(true);
        } else if (currentDifficulty.equals(getString(R.string.settings_radio_medium))) {
            radioButtonMedium.setChecked(true);
        } else {
            radioButtonEasy.setChecked(true);
        }
        radioGroupDifficulty = view.findViewById(R.id.radioGroupDifficulty);
        radioGroupDifficulty.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                playSound(R.raw.click_settings);
                String newDiff;
                if (checkedId == R.id.radioButtonEasy) {
                    newDiff = getString(R.string.settings_radio_easy);
                } else if (checkedId == R.id.radioButtonMedium) {
                    newDiff = getString(R.string.settings_radio_medium);
                } else {
                    newDiff = getString(R.string.settings_radio_hard);
                }
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(getString(R.string.preferences_difficulty), newDiff).apply();
                if (!newDiff.equalsIgnoreCase(currentDifficulty)) {
                    currentDifficulty = newDiff;
                    mListener.onChangeDifficulty();
                }
            }
        });
        imageViewCloseSettings.setOnClickListener(v -> {
            playSound(R.raw.click_settings);
            mListener.onBackButtonSettings();
        });
        textViewReset.setOnClickListener(v -> {
            playSound(R.raw.click_settings);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle(R.string.alert_reset_game_title);
            alertDialogBuilder.setMessage(R.string.alert_reset_game_text);
            alertDialogBuilder.setPositiveButton(R.string.alert_reset_game_positive,
                    (dialog, which) -> {
                        playSound(R.raw.click_settings);
                        mListener.onGameReset();
            });
            alertDialogBuilder.setNegativeButton(R.string.alert_reset_game_cancel, null);
            alertDialogBuilder.setCancelable(false).show();
        });
    }

    private void handleBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mListener.onBackButtonSettings();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    public interface ActionListener {
        void onBackButtonSettings();
        void onChangeDifficulty();
        void onChangeUsername(String username);
        void onGameReset();
    }

    private void playSound(int resID) {
        MediaPlayer mp = MediaPlayer.create(getContext(), resID);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }
}