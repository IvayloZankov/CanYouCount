package com.fosents.canyoucount;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsFragment extends Fragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    ImageView imageViewBackground;
    RadioGroup radioGroupDifficulty;
    RadioButton radioButtonEasy, radioButtonMedium, radioButtonHard;
    ActionListener mListener;
    String currentDifficulty;

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
        imageViewBackground.setOnClickListener(v -> {});
        currentDifficulty = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.settings_difficulty_select), getString(R.string.settings_radio_easy));
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
                String newDiff;
                if (checkedId == R.id.radioButtonEasy) {
                    newDiff = getString(R.string.settings_radio_easy);
                } else if (checkedId == R.id.radioButtonMedium) {
                    newDiff = getString(R.string.settings_radio_medium);
                } else {
                    newDiff = getString(R.string.settings_radio_hard);
                }
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(getString(R.string.settings_difficulty_select), newDiff).apply();
                if (!newDiff.equalsIgnoreCase(currentDifficulty)) {
                    currentDifficulty = newDiff;
                    mListener.onChangeDifficulty();
                }
            }
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
    }
}