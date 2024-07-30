package com.fosents.canyoucount;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SettingsFragment.ActionListener {

    final int START_LEVEL = 1;
    final int START_SCORE = 0;
    final int OPERATION_BARRIER = 3;
    final int PROGRESS_FOR_LEVEL = 5;

    private TextView textViewLevel, correctsView, errorsView, timeView, numberOneView, numberTwoView;
    private TextView operationView, textViewStartButton, textViewOnFail, textViewUsername;
    private TextView textViewScorePlaceholder;
    private LinearLayout startButton;
    private LinearLayout taskLayout;
    private GridLayout answersGridLayout;
    private int mFirstNumRange, mSecondNumRange, mAnswersMax, numberOne, numberTwo, answerCellNumber;
    private int corrects, wrongs, level, progress, correctAnswer, timeMillis, score;
    private int[] colors;
    private String username;

    private SharedPreferences prefs;
    private Operation operation;
    private CountDownTimer timer;
    private Handler handler;
    private Runnable runnable;
    private ImageView imageViewSettings, imageViewRefresh, imageViewEditUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.e("MainActivity", "onCreate");

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        handler = new Handler(getMainLooper());
        numberOneView = findViewById(R.id.number1);
        numberTwoView = findViewById(R.id.number2);
        operationView = findViewById(R.id.operation);
        answersGridLayout = findViewById(R.id.gridView);
        textViewOnFail = findViewById(R.id.timeOutText);
        textViewLevel = findViewById(R.id.levelNumber);
        correctsView = findViewById(R.id.scoreNumber);
        errorsView = findViewById(R.id.errorsNumber);
        timeView = findViewById(R.id.timeNumber);
        startButton = findViewById(R.id.startButtonLayout);
        taskLayout = findViewById(R.id.taskLayout);
        textViewStartButton = findViewById(R.id.textViewStartButton);
        imageViewSettings = findViewById(R.id.imageViewSettings);
        imageViewRefresh = findViewById(R.id.imageViewRefresh);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewScorePlaceholder = findViewById(R.id.textViewScorePlaceholder);
        imageViewEditUsername = findViewById(R.id.imageViewEditUsername);

        progress = 0;
        operation = Operation.ADDITION;
        colors = new int[]{
                R.color.beige, R.color.pink, R.color.yellow,
                R.color.light_purple, R.color.very_light_grey, R.color.light_blue,
                R.color.light_red, R.color.yellow_green, R.color.light_green
        };
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        username = prefs.getString(getString(R.string.preferences_username), getString(R.string.preferences_username_default));
        if (username.equals(getString(R.string.preferences_username_default))) {
            username = getString(R.string.counter, "" + (100000 + new Random().nextInt(900000)));
            prefs.edit().putString(getString(R.string.preferences_username), username).apply();
        }
        textViewUsername.setText(username);
        score = prefs.getInt(getString(R.string.preferences_score), START_SCORE);
        textViewScorePlaceholder.setText(String.valueOf(score));
        level = prefs.getInt(getString(R.string.preferences_level), START_LEVEL);
        textViewLevel.setText(String.valueOf(level));
        setAnswersGridLayout();
        answersGridLayout.setAlpha((float) 0.5);
        startButton.setOnClickListener(v -> {
            playSound(R.raw.start);
            animateClick(startButton);
            startButton.setEnabled(false);
            new Handler(getMainLooper()).postDelayed(() -> {
                Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                taskLayout.setAnimation(animFadeIn);
                taskLayout.setVisibility(View.VISIBLE);
                answersGridLayout.setAlpha((float) 1.0);
                startButton.setVisibility(View.INVISIBLE);
                textViewOnFail.clearAnimation();
                textViewOnFail.setVisibility(View.INVISIBLE);
                resetScore();
                startTimer();
                setNewBoard();
                Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                startButton.setAnimation(animFadeOut);
                imageViewRefresh.setOnClickListener(view -> {
                    playSound(R.raw.start);
                    animateClick(imageViewRefresh);
                    stopTimer();
                    new Handler(getMainLooper()).postDelayed(() -> {
                        resetScore();
                        resetBoard();
                    }, 300);
                });
            }, 1000);
        });
        imageViewSettings.setOnClickListener(v -> {
            playSound(R.raw.click_settings);
            SettingsFragment settings = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG);
            if (settings != null && settings.isVisible()) {
                imageViewSettings.animate().rotation(0);
                closeSettingsFragment(settings);
            } else {
                imageViewSettings.animate().rotation(100);
                initSettingsFragment();
            }
        });
        imageViewEditUsername.setOnClickListener(v -> {
            playSound(R.raw.click_settings);
            initSettingsFragment();
        });
    }

    private void setAnswersGridLayout() {
        String difficulty = prefs.getString(getString(R.string.preferences_difficulty),
                getString(R.string.preferences_difficulty_default));
        if (difficulty.equals(getString(R.string.preferences_difficulty_default))) {
            difficulty = getString(R.string.settings_radio_easy);
            prefs.edit().putString(getString(R.string.preferences_difficulty), difficulty).apply();
        }
//        Log.e("DIFF", difficulty);
        int viewsCount;
        answersGridLayout.removeAllViews();
        if (difficulty.equals(getString(R.string.settings_radio_hard))) {
            timeMillis = 30100;
            answersGridLayout.setColumnCount(3);
            answersGridLayout.setRowCount(3);
            viewsCount = 9;
        } else if (difficulty.equals(getString(R.string.settings_radio_medium))) {
            timeMillis = 45100;
            answersGridLayout.setColumnCount(3);
            answersGridLayout.setRowCount(2);
            viewsCount = 6;
        } else {
            timeMillis = 60100;
            answersGridLayout.setColumnCount(2);
            answersGridLayout.setRowCount(2);
            viewsCount = 4;
        }
        for (int i = 0; i < viewsCount; i++) {
            FrameLayout frame = (FrameLayout) getLayoutInflater().inflate(R.layout.answer_frame, null);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            );
            params.height = 0;
            params.width = 0;
            params.topMargin = 25;
            params.bottomMargin = 25;
            params.leftMargin = 25;
            params.rightMargin = 25;
            frame.setLayoutParams(params);
            if (difficulty.equals(getString(R.string.settings_radio_hard))) {
                ((TextView) frame.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            }
            answersGridLayout.addView(frame);
        }
        setBoardColors();
        resetScore();
        timeView.setText(String.valueOf(timeMillis / 1000));
    }

    private void setNewBoard() {
        setBoardColors();
        if (progress == PROGRESS_FOR_LEVEL) {
            if (corrects > wrongs) {
                score = calculateScore();
                this.level++;
                textViewScorePlaceholder.setText(String.valueOf(score));
                prefs.edit().putInt(getString(R.string.preferences_level), level).apply();
                prefs.edit().putInt(getString(R.string.preferences_score), score).apply();
                textViewLevel.setText(String.valueOf(this.level));
                bounceView(textViewLevel);
                bounceView(textViewScorePlaceholder);
            }
            startTimer();
            resetScore();
        }
        if (this.level >= OPERATION_BARRIER) {
            int random = getRandomNumber(0, 2);
            operation = random == 0 ? Operation.ADDITION : Operation.SUBTRACTION;
        }

        if (operation == Operation.ADDITION) {
            mFirstNumRange = level + 3;
            mSecondNumRange = level + 3;
            mAnswersMax = 9 + level;
            numberOne = getRandomNumber(level - 1, mFirstNumRange);
            if (this.level < 5) {
                numberTwo = getRandomNumber(0, mSecondNumRange);
            } else {
                numberTwo = getRandomNumber(level - 5, mSecondNumRange);
            }
            operationView.setText("+");
            correctAnswer = numberOne + numberTwo;
        } else {
            mFirstNumRange = level + 3;
            mSecondNumRange = mFirstNumRange;
            mAnswersMax = mFirstNumRange + level;
            numberOne = getRandomNumber(level - 1, mFirstNumRange);
            if (this.level < 5) {
                numberTwo = getRandomNumber(0, numberOne);
            } else {
                numberTwo = getRandomNumber(level - 5, numberOne);
            }
            operationView.setText("-");
            correctAnswer = numberOne - numberTwo;
        }

        numberOneView.setText(String.valueOf(numberOne));
        numberTwoView.setText(String.valueOf(numberTwo));

        int[] numbersSet = new int[answersGridLayout.getChildCount()];
        answerCellNumber = new Random().nextInt(answersGridLayout.getChildCount());
        numbersSet[answerCellNumber] = correctAnswer;
        FrameLayout correctFrame = (FrameLayout) answersGridLayout.getChildAt(answerCellNumber);
        TextView numberView = (TextView) correctFrame.getChildAt(0);
        numberView.setText(String.valueOf(correctAnswer));

        correctFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_round);
                if (drawable != null) {
                    drawable.setTint(getResources().getColor(R.color.green));
                    correctFrame.setBackground(drawable);
                }
                animateClick(correctFrame);
                correctAnswer();
            }
        });

        for (int i = 0; i < answersGridLayout.getChildCount(); i++) {
            if (i != answerCellNumber) {
                int randomNumber = new Random().nextInt(mAnswersMax);
                boolean duplicate = true;
                while (duplicate) {
                    boolean found = false;
                    if (i == 0 && randomNumber == correctAnswer) {
                        randomNumber = new Random().nextInt(mAnswersMax);
                        found = true;
                    } else {
                        for (int j = 0; j < i; j++) {
                            if (randomNumber == correctAnswer || numbersSet[j] == randomNumber) {
                                randomNumber = new Random().nextInt(mAnswersMax);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        duplicate = false;
                    }
                }
                numbersSet[i] = randomNumber;
                FrameLayout frame = (FrameLayout) answersGridLayout.getChildAt(i);
                TextView number = (TextView) frame.getChildAt(0);
                number.setText(String.valueOf(randomNumber));
                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_round);
                        if (drawable != null) {
                            TypedValue typedValue = new TypedValue();
                            getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
                            drawable.setTint(getResources().getColor(typedValue.resourceId));
                            frame.setBackground(drawable);
                        }
                        frame.setOnClickListener(null);
                        animateClick(frame);
                        wrongAnswer();
                    }
                });
            }
        }
    }

    private int calculateScore() {
        int newScore = score;
        String difficulty = prefs.getString(getString(R.string.preferences_difficulty),
                getString(R.string.preferences_difficulty_default));
        int answerDiff = corrects - wrongs;
        int scoreToAdd = 0;
        if (difficulty.equals(getString(R.string.settings_radio_easy))) {
            switch (answerDiff) {
                case 1:
                    scoreToAdd = 1 + level;
                    break;
                case 2:
                    scoreToAdd = 3 + level;
                    break;
                case 3:
                    scoreToAdd = 6 + level;
                    break;
                case 4:
                    scoreToAdd = 10 + level;
                    break;
                case 5:
                    scoreToAdd = 15 + level;
                    break;
            }
        }
        if (difficulty.equals(getString(R.string.settings_radio_medium))) {
            int levelAdd = level * 2;
            switch (answerDiff) {
                case 1:
                    scoreToAdd = 2 + levelAdd;
                    break;
                case 2:
                    scoreToAdd = 5 + levelAdd;
                    break;
                case 3:
                    scoreToAdd = 9 + levelAdd;
                    break;
                case 4:
                    scoreToAdd = 14 + levelAdd;
                    break;
                case 5:
                    scoreToAdd = 20 + levelAdd;
                    break;
            }
        }
        if (difficulty.equals(getString(R.string.settings_radio_hard))) {
            int levelAdd = level * 3;
            switch (answerDiff) {
                case 1:
                    scoreToAdd = 3 + levelAdd;
                    break;
                case 2:
                    scoreToAdd = 7 + levelAdd;
                    break;
                case 3:
                    scoreToAdd = 12 + levelAdd;
                    break;
                case 4:
                    scoreToAdd = 18 + levelAdd;
                    break;
                case 5:
                    scoreToAdd = 25 + levelAdd;
                    break;
            }
        }
        Log.e("SCORE TO ADD", String.valueOf(scoreToAdd));
        return newScore + scoreToAdd;
    }

    private void correctAnswer() {
        playSound(R.raw.correct);
        progress++;
        corrects++;
        if (progress == PROGRESS_FOR_LEVEL) {
            stopTimer();
        }
        correctsView.setTextColor(getResources().getColor(R.color.dark_green));
        correctsView.setText(String.valueOf(corrects));
        bounceView(correctsView);
        disableGridListeners();
        runnable = () -> setNewBoard();
        handler.postDelayed(runnable, 1000);
    }

    private void wrongAnswer() {
        playSound(R.raw.wrong);
        wrongs++;
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        errorsView.setTextColor(getResources().getColor(typedValue.resourceId));
        errorsView.setText(String.valueOf(wrongs));
        if (wrongs >= PROGRESS_FOR_LEVEL) {
            disableGridListeners();
            taskLayout.setVisibility(View.INVISIBLE);
            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            taskLayout.setAnimation(animFadeOut);
            answersGridLayout.setAlpha((float) 0.5);
            textViewStartButton.setText(R.string.try_again);
            startButton.setVisibility(View.VISIBLE);
            Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            startButton.setAnimation(animFadeIn);
            textViewOnFail.setText(R.string.wrong_attempts);
            textViewOnFail.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
            textViewOnFail.setVisibility(View.VISIBLE);
            bounceView(textViewOnFail);
            stopTimer();
            handler.removeCallbacks(runnable);
            startButton.setEnabled(true);
        }
    }

    private void bounceView(View view) {
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        anim.setInterpolator(interpolator);
        view.startAnimation(anim);
    }

    private void animateClick(View view) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(
                () -> view.animate().scaleX(1).scaleY(1).setDuration(100));
    }

    public int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private void setBoardColors() {
        for (int i = 0; i < answersGridLayout.getChildCount(); i++) {
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_round);
            drawable.setTint(getResources().getColor(colors[i]));
            answersGridLayout.getChildAt(i).setBackground(drawable);
        }
    }

    private void resetScore() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
        progress = 0;
        if (corrects != 0 || wrongs != 0) {
            corrects = 0;
            wrongs = 0;
            correctsView.setTextColor(getResources().getColor(typedValue.resourceId));
            correctsView.setText(String.valueOf(corrects));
            bounceView(correctsView);
            errorsView.setTextColor(getResources().getColor(typedValue.resourceId));
            errorsView.setText(String.valueOf(wrongs));
            bounceView(errorsView);
        }
    }

    private void startTimer() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
        timeView.setTextColor(getResources().getColor(typedValue.resourceId));

        timer =  new CountDownTimer(timeMillis, 1000){

            long remainingTime = timeMillis;

            boolean firstTick = true;

            @Override
            public void onTick(long millisUntilFinished) {
                if (firstTick) {
                    firstTick = false;
                } else {
                    remainingTime -= 1000;
                }
                displayRemainSecs(remainingTime);
            }

            @Override
            public void onFinish() {
                remainingTime -= 1000;
                displayRemainSecs(remainingTime);
                disableGridListeners();
                taskLayout.setVisibility(View.INVISIBLE);
                Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                taskLayout.setAnimation(animFadeOut);
                answersGridLayout.setAlpha((float) 0.5);
                textViewStartButton.setText(R.string.try_again);
                startButton.setVisibility(View.VISIBLE);
                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                startButton.setAnimation(animFadeIn);
                textViewOnFail.setText(R.string.time_out);
                textViewOnFail.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
                textViewOnFail.setVisibility(View.VISIBLE);
                bounceView(textViewOnFail);
                handler.removeCallbacks(runnable);
                startButton.setEnabled(true);
            }
        }.start();
    }

    private void resetBoard() {
        if (taskLayout.getVisibility() == View.VISIBLE) {
            taskLayout.setVisibility(View.INVISIBLE);
            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            taskLayout.setAnimation(animFadeOut);
            startButton.setVisibility(View.VISIBLE);
            Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            startButton.setAnimation(animFadeIn);
            startButton.setEnabled(true);
        }
        textViewStartButton.setText(R.string.start);
        textViewOnFail.setVisibility(View.GONE);
        timeView.setText(String.valueOf(timeMillis / 1000));
        setAnswersGridLayout();
    }

    private void displayRemainSecs(long remainingTime) {
        if (remainingTime < 4000) {
            timeView.setText(String.valueOf(remainingTime / 1000));
            timeView.clearAnimation();
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
            timeView.setTextColor(getResources().getColor(typedValue.resourceId));
            bounceView(timeView);
        } else {
            timeView.setText(String.valueOf(remainingTime / 1000));
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void disableGridListeners() {
        for (int i = 0; i < answersGridLayout.getChildCount(); i++) {
            answersGridLayout.getChildAt(i).setOnClickListener(null);
        }
    }

    private void playSound(int resID) {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), resID);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }

    private void initSettingsFragment() {
        imageViewEditUsername.setOnClickListener(null);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(android.R.id.content, SettingsFragment.newInstance(), SettingsFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void closeSettingsFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.remove(fragment).commit();
        imageViewEditUsername.setOnClickListener(v -> {
            playSound(R.raw.click_settings);
            initSettingsFragment();
        });
//        setAnswersGridLayout();
    }

    @Override
    public void onBackButtonSettings() {
        SettingsFragment settings = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG);
        if (settings != null && settings.isVisible()) {
            imageViewSettings.animate().rotation(0);
            closeSettingsFragment(settings);
        }
    }

    @Override
    public void onChangeDifficulty() {
        stopTimer();
        resetScore();
        resetBoard();
    }

    @Override
    public void onChangeUsername(String username) {
        textViewUsername.setText(username);
    }

    @Override
    public void onGameReset() {
        level = START_LEVEL;
        score = START_SCORE;
        stopTimer();
        resetScore();
        resetBoard();
        prefs.edit().putInt(getString(R.string.preferences_level), level).apply();
        prefs.edit().putInt(getString(R.string.preferences_score), score).apply();
        textViewScorePlaceholder.setText(String.valueOf(score));
        textViewLevel.setText(String.valueOf(this.level));
        bounceView(textViewLevel);
        bounceView(textViewScorePlaceholder);
    }

//    private Fragment getVisibleFragment() {
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        for (Fragment fragment : fragments) {
//            if (fragment != null && fragment.isVisible())
//                return fragment;
//        }
//        return null;
//    }
}