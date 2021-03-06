package com.faforever.client.util;

import com.faforever.client.i18n.I18n;
import com.faforever.client.preferences.ChatPrefs;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.preferences.TimeInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.TimeZone;


@Lazy
@Service
public class TimeServiceImpl implements TimeService {

  private final I18n i18n;
  private final PreferencesService preferencesService;

  @Inject
  public TimeServiceImpl(I18n i18n, PreferencesService preferencesService) {
    this.i18n = i18n;
    this.preferencesService = preferencesService;
  }

  @Override
  public String timeAgo(Temporal temporal) {
    if (temporal == null) {
      return "";
    }

    Duration ago = Duration.between(temporal, Instant.now());

    if (Duration.ofMinutes(1).compareTo(ago) > 0) {
      return i18n.getQuantized("secondAgo", "secondsAgo", ago.getSeconds());
    }
    if (Duration.ofHours(1).compareTo(ago) > 0) {
      return i18n.getQuantized("minuteAgo", "minutesAgo", ago.toMinutes());
    }
    if (Duration.ofDays(1).compareTo(ago) > 0) {
      return i18n.getQuantized("hourAgo", "hoursAgo", ago.toHours());
    }
    if (Duration.ofDays(30).compareTo(ago) > 0) {
      return i18n.getQuantized("dayAgo", "daysAgo", ago.toDays());
    }
    if (Duration.ofDays(365).compareTo(ago) > 0) {
      return i18n.getQuantized("monthAgo", "monthsAgo", ago.toDays() / 30);
    }

    return i18n.getQuantized("yearAgo", "yearsAgo", ago.toDays() / 365);
  }

  @Override
  public String lessThanOneDayAgo(Temporal temporal) {
    if (temporal == null) {
      return "";
    }

    Duration ago = Duration.between(temporal, Instant.now());

    if (ago.compareTo(Duration.ofDays(1)) <= 0) {
      return timeAgo(temporal);
    }

    return asDate(temporal);
  }

  @Override
  public String asDate(TemporalAccessor temporalAccessor) {
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(getCurrentTimeLocale())
        .withZone(TimeZone.getDefault().toZoneId())
        .format(temporalAccessor);
  }

  @Override
  public String asShortTime(Temporal temporal) {
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(getCurrentTimeLocale())
        .format(ZonedDateTime.ofInstant(Instant.from(temporal), TimeZone.getDefault().toZoneId()));
  }

  @Override
  public String asIsoTime(Temporal temporal) {
    return DateTimeFormatter.ISO_TIME.format(temporal);
  }

  private Locale getCurrentTimeLocale() {
    ChatPrefs chatPrefs = preferencesService.getPreferences().getChat();
    if (chatPrefs.getTimeFormat().equals(TimeInfo.AUTO)) {
      return Locale.getDefault();
    }
    return preferencesService.getPreferences().getChat().getTimeFormat().getUsedLocale();

  }

  @Override
  public String shortDuration(Duration duration) {
    if (duration == null) {
      return "";
    }

    if (Duration.ofMinutes(1).compareTo(duration) > 0) {
      return i18n.get("duration.seconds", duration.getSeconds());
    }
    if (Duration.ofHours(1).compareTo(duration) > 0) {
      return i18n.get("duration.minutesSeconds", duration.toMinutes(), duration.getSeconds() % 60);
    }

    return i18n.get("duration.hourMinutes", duration.toMinutes() / 60, duration.toMinutes() % 60);
  }

  @Override
  public String asHms(Duration duration) {
    long seconds = duration.getSeconds();
    return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
  }
}
