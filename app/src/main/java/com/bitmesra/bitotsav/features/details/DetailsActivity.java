package com.bitmesra.bitotsav.features.details;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitmesra.bitotsav.R;
import com.bitmesra.bitotsav.database.models.details.EventDto;
import com.bitmesra.bitotsav.features.EventDtoType;
import com.bitmesra.bitotsav.ui.AchievementHelper;
import com.bitmesra.bitotsav.ui.CustomTextView;
import com.bitmesra.bitotsav.utils.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends AppCompatActivity implements DetailsViewInterface {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    CustomTextView toolbarTitle;

    String eventName;
    String eventId;
    int eventDtoType;
    boolean fetch = true;
    DetailsPresenter presenter;
    @BindView(R.id.detail_time_venue)
    CustomTextView timeVenue;
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.detail_desc)
    CustomTextView desc;
    @BindView(R.id.detail_rules)
    CustomTextView rules;
    @BindView(R.id.detail_money)
    CustomTextView money;
    @BindView(R.id.detail_points)
    CustomTextView points;
    @BindView(R.id.detail_participants)
    CustomTextView participants;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.star_subscribe)
    FloatingActionButton subscribeButton;
    @BindView(R.id.frame_image)
    ImageView frame_image;
    @BindView(R.id.background_image)
    ImageView background_image;
    @BindView(R.id.mario_loading_image)
    ImageView marioLoadingImage;
    @BindView(R.id.mario_loading_text)
    CustomTextView marioLoadingText;
    @BindView(R.id.achievemnt_loading_holder)
    View achievemntHolder;

    private boolean firstTime = true;
    private AchievementHelper achievementHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        eventName = getIntent().getStringExtra("eventName");
        eventId = getIntent().getStringExtra("id");
        eventDtoType = getIntent().getIntExtra("eventDtoType", EventDtoType.TYPE_FLAGSHIP);
        fetch = getIntent().getBooleanExtra("fetchNetwork", true);
        firstTime = getIntent().getBooleanExtra("firstTime", true);

        presenter = new DetailsPresenter(this, this);
        achievementHelper = new AchievementHelper(this, achievemntHolder, marioLoadingImage, marioLoadingText);
        presenter.getDetailsDtoFromRealm(eventName);

        if (eventDtoType == EventDtoType.TYPE_FLAGSHIP) {
            String storedImageName = presenter.getImageName(eventName);
            if (storedImageName != null) {
                background_image.setImageDrawable(getResources().getDrawable(
                        getResources().getIdentifier(storedImageName, "drawable", getPackageName())
                ));
            }
        }
        animate();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            presenter.fetchDetailsDto(eventName, eventId, eventDtoType);
        });
        if (fetch) {
            presenter.fetchDetailsDto(eventName, eventId, eventDtoType);
        }
        if (presenter.isTopicSubscribed(eventName)) {
            subscribeButton.setImageDrawable(getDrawable(R.drawable.ic_bell));
        } else {
            subscribeButton.setImageDrawable(getDrawable(R.drawable.ic_no_bell));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateDetailView(EventDto eventDto) {
        if (eventDto.getName() != null) {
            if (!eventDto.getName().trim().isEmpty()) {
                toolbarTitle.setText(eventDto.getName());
            }
        }
        if (eventDto.getDescription() != null) {
            desc.setText(eventDto.getDescription());
        }
        if (eventDto.getTime() != null && eventDto.getVenue() != null) {
            if (!eventDto.getTime().trim().isEmpty() && !eventDto.getVenue().trim().isEmpty()) {
                timeVenue.setVisibility(View.VISIBLE);
                timeVenue.setText(eventDto.getTime() + " at " + eventDto.getVenue());
            } else {
                timeVenue.setVisibility(View.GONE);
            }
        }
        if (eventDto.getMoney() != null) {
            if (!eventDto.getMoney().trim().isEmpty()) {
                money.setVisibility(View.VISIBLE);
                money.setText("Prize Money: " + eventDto.getMoney());
            } else {
                money.setVisibility(View.GONE);
            }
        }
        if (eventDto.getRules() != null) {
            if (!eventDto.getRules().trim().isEmpty()) {
                rules.setVisibility(View.VISIBLE);
                Spannable spannable = new SpannableString("Rules:\n" + eventDto.getRules());
                spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.card_text)), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                rules.setText(spannable, TextView.BufferType.SPANNABLE);
            } else {
                rules.setVisibility(View.GONE);
            }
        }
        if (eventDto.getPoints() != null) {
            if (!eventDto.getPoints().trim().isEmpty()) {
                Spannable spannable = new SpannableString("Points:\n" + eventDto.getPoints());
                spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                points.setText(spannable, TextView.BufferType.SPANNABLE);
                points.setVisibility(View.VISIBLE);
            } else {
                points.setVisibility(View.GONE);
            }
        }
        if (eventDto.getParticipantsCount() != null) {
            if (!eventDto.getParticipantsCount().trim().isEmpty()) {
                participants.setVisibility(View.VISIBLE);
                String s = "No. of participants: ";
                Spannable spannable = new SpannableString(s + eventDto.getParticipantsCount());
                spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                participants.setText(spannable, TextView.BufferType.SPANNABLE);
            } else {
                participants.setVisibility(View.GONE);
            }
        }
        if (eventDto.getReg() != null) {
            if (!eventDto.getReg().trim().isEmpty()) {
                desc.setText(desc.getText() + "\n\n" + eventDto.getReg());
            }
        }
        if (eventDto.getImageurl() != null) {
            if (!eventDto.getImageurl().trim().isEmpty()) {
                String imageurl = eventDto.getImageurl();
                Log.d("tag", imageurl);
                if (!(eventDtoType == EventDtoType.TYPE_FLAGSHIP)) {
                    Picasso.with(this)
                            .load(imageurl)
                            .noPlaceholder()
                            .into(background_image);
                }
            }
        }
    }

    @Override
    public void partialUpdateDetailView() {
        toolbarTitle.setText(eventName);
        desc.setText(presenter.getDescription(eventName));
        timeVenue.setVisibility(View.GONE);
        money.setVisibility(View.GONE);
        rules.setVisibility(View.GONE);
        points.setVisibility(View.GONE);
        participants.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.removeChangeListener(eventName);
    }

    @OnClick(R.id.star_subscribe)
    void onSubscribe() {
        if (presenter.isTopicSubscribed(eventName)) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(eventName.replaceAll(" ", ""));
            subscribeButton.setImageDrawable(getDrawable(R.drawable.ic_no_bell));
            Snackbar.make(desc, "You have unsubscribed from " + eventName, Snackbar.LENGTH_LONG).show();
            presenter.unsubscribeFromTopic(eventName);
            setResult(21);
        } else {
            subscribeButton.setImageDrawable(getDrawable(R.drawable.ic_bell));
            Snackbar.make(desc, "You will now receive all FUTURE UPDATES for " + eventName, Snackbar.LENGTH_LONG).show();
            FirebaseMessaging.getInstance().subscribeToTopic(eventName.replaceAll(" ", ""));
            presenter.subscribeToTopic(eventName);
            setResult(21);
        }
    }


    @Override
    public void showAchievment() {
        refreshLayout.setRefreshing(true);
        achievementHelper.startLoading();
    }

    @Override
    public void hideAchievment() {
        refreshLayout.setRefreshing(false);
        achievementHelper.stopLoading();
    }

    @Override
    public void errorAchievment() {
        refreshLayout.setRefreshing(false);
        achievementHelper.errorLoading();
    }


    private void animate() {
        toolbarTitle.setAlpha(0f);
        toolbarTitle.animate().alpha(1f).setDuration(1000).start();
        frame_image.setAlpha(0f);
        frame_image.animate().alpha(1f).setDuration(1000).start();
        timeVenue.setTranslationY(-200.0f);
        timeVenue.animate().translationY(0f).setDuration(1000).start();
        desc.setTranslationY(Utils.getScreenHeight(this));
        desc.animate().translationY(0).setDuration(1000).start();
        money.setTranslationY(Utils.getScreenHeight(this));
        money.animate().translationY(0).setDuration(1100).start();
        divider.setTranslationY(Utils.getScreenHeight(this));
        divider.animate().translationY(0).setDuration(1200).start();
        rules.setTranslationY(Utils.getScreenHeight(this));
        rules.animate().translationY(0).setDuration(1300).start();
        points.setTranslationY(Utils.getScreenHeight(this));
        points.animate().translationY(0).setDuration(1400).start();
        participants.setTranslationY(Utils.getScreenHeight(this));
        participants.animate().translationY(0).setDuration(1500).start();
    }
}
