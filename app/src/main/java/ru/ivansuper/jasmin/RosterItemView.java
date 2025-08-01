//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.TextView;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPGroup;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQGroup;
import ru.ivansuper.jasmin.icq.qip_statuses;
import ru.ivansuper.jasmin.icq.xstatus;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JGroup;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.protocols.IMProfile;

/**
 * Represents a single item in the roster (contact list).
 * <p>
 * This class is responsible for displaying information about a contact, group, profile,
 * conference, or splitter in the roster. It handles rendering of the item's name,
 * status, avatar, and other visual elements.
 * <p>
 * The appearance and behavior of the RosterItemView are determined by its `type`
 * and the data provided in the {@link #update(ContactlistItem)} method.
 * <p>
 * Key features:
 * - Displays different types of roster items:
 *   - {@link #TYPE_CONTACT}: Regular contact.
 *   - {@link #TYPE_GROUP}: A group of contacts.
 *   - {@link #TYPE_PROFILE}: The user's own profile.
 *   - {@link #TYPE_CONFERENCE}: A chat conference.
 *   - {@link #TYPE_SPLITTER}: A visual separator in the list.
 * - Shows contact name, status icon, and optional status text.
 * - Displays avatars for contacts if enabled and available.
 * - Indicates unread messages with a blinking effect and/or a counter.
 * - Shows client icons if enabled and available.
 * - Displays visibility status (e.g., visible, invisible).
 * - Shows ignore status.
 * - Handles typing notifications.
 * - Adapts its layout and appearance based on the item type and available space.
 * - Uses {@link TextPaint} for efficient text rendering.
 * - Manages blinking animations for unread messages or important events.
 */
public class RosterItemView extends View {
    /** @noinspection unused*/
    public static final int AVATAR_PADDING = 3;
    public static final int AVATAR_SIZE = 40;
    public static final int BLINK_INTERVAL_MILLIS = 500;
    public static final int BLINK_TIME_MILLIS = 15000;
    /** @noinspection unused*/
    private static final int HORIZONTAL_PADDING = 3;
    /** @noinspection unused*/
    private static final int ICON_HPADDING = 3;
    public static final int MODULATOR_SPEED = 10;
    /** @noinspection unused*/
    private static final int TEXT_PADDING = 3;
    /** @noinspection unused*/
    public static final int TYPE_CONFERENCE = 3;
    /** @noinspection unused*/
    public static final int TYPE_CONTACT = 0;
    /** @noinspection unused*/
    public static final int TYPE_GROUP = 1;
    /** @noinspection unused*/
    public static final int TYPE_PROFILE = 2;
    /** @noinspection unused*/
    public static final int TYPE_SPLITTER = 4;
    /** @noinspection unused*/
    private static final int VERTICAL_PADDING = 3;
    private float av_h;
    private int available_width = 0;
    private Drawable avatar;
    private final NinePatchDrawable avatar_back;
    /** @noinspection FieldCanBeLocal*/
    private final Paint avatar_border_;
    private Rect avatar_bounds;
    private final Path avatar_path;
    private boolean blink_posted = false;
    private boolean blink_visible = false;
    private long blink_zero_ts;
    private Drawable client;
    private final TextPaint counter_;
    private int direction_ = 10;
    private boolean draw_avatar;
    private boolean draw_client;
    private boolean draw_status_desc;
    private final Paint effect;
    private Drawable ex_status;
    private boolean has_unreaded;
    private float icons_center;
    private Drawable ignore;
    private float lbl_left;
    private float lbl_top;
    private TextView mStatusDrawer;
    private String name;
    private final TextPaint name_;
    private int online;
    private boolean opened;
    private Drawable status;
    private String status_text;
    private final TextPaint status_text_;
    private float sts_left;
    private float sts_top;
    private int total;
    private int type;
    private boolean typing;
    private int unreaded_count = 0;
    private boolean use_blink;
    private int value_ = 0;
    private Drawable visibility;

    public RosterItemView(Context var1) {
        super(var1);
        int var2 = (int)(40.0F * resources.dm.density);
        this.avatar_back = (NinePatchDrawable)this.getContext().getResources().getDrawable(R.drawable.avatar_back);
        this.avatar_back.setBounds(0, 0, var2, var2);
        Rect padding = new Rect();
        this.avatar_back.getPadding(padding);
        this.avatar_bounds = new Rect(padding.left, padding.top, var2 - padding.right, var2 - padding.bottom);
        this.name_ = new TextPaint();
        this.name_.setAntiAlias(true);
        this.status_text_ = new TextPaint();
        this.status_text_.setAntiAlias(true);
        this.counter_ = new TextPaint();
        this.counter_.setAntiAlias(true);
        this.effect = new TextPaint();
        this.effect.setColor(-1);
        this.effect.setStyle(Style.FILL_AND_STROKE);
        this.effect.setAntiAlias(true);
        this.effect.setStrokeWidth(3.0F);
        this.avatar_path = new Path();
        updateAvatarPath();
        this.avatar_border_ = new Paint();
        this.avatar_border_.setColor(2013265919);
        this.avatar_border_.setStrokeWidth(0.0F);
        this.avatar_border_.setStyle(Style.FILL);
        this.avatar_border_.setAntiAlias(true);
        this.setWillNotDraw(true);
        this.setDrawingCacheEnabled(false);
        this.setWillNotCacheDrawing(true);
    }

    private float calculateNameAvail() {
        float labelWidth = (float)(this.available_width - 3) - this.lbl_left;
        float adjustedLabelWidth = labelWidth;
        if (this.draw_client) {
            adjustedLabelWidth = labelWidth;
            if (this.client != null) {
                adjustedLabelWidth = labelWidth - (float)this.client.getBounds().width();
            }
        }

        labelWidth = adjustedLabelWidth;
        if (this.visibility != null) {
            labelWidth = adjustedLabelWidth - (float)this.visibility.getIntrinsicWidth();
        }

        adjustedLabelWidth = labelWidth;
        if (this.ignore != null) {
            adjustedLabelWidth = labelWidth - (float)this.ignore.getIntrinsicWidth();
        }

        return adjustedLabelWidth;
    }

    private void checkNeedBlinking() {
        this.use_blink = Math.abs(System.currentTimeMillis() - this.blink_zero_ts) <= BLINK_TIME_MILLIS;
    }

    public static void computeBounds(Drawable boundsProvider, Rect outRect) {
        int outRectLeft = outRect.left;
        int outRectTop = outRect.top;
        int outRectWidth = outRect.width();
        int outRectHeight = outRect.height();
        float scaleFactor = (float)outRectWidth / (float)Math.max(boundsProvider.getIntrinsicWidth(), boundsProvider.getIntrinsicHeight());
        int scaledWidth = (int)((float)boundsProvider.getIntrinsicWidth() * scaleFactor);
        int scaledHeight = (int)((float)boundsProvider.getIntrinsicHeight() * scaleFactor);
        outRect.left = outRectWidth / 2 + outRectLeft - scaledWidth / 2;
        outRect.top = outRectHeight / 2 + outRectTop - scaledHeight / 2;
        outRect.right = outRect.left + scaledWidth;
        outRect.bottom = outRect.top + scaledHeight;
    }

    private void drawIcon(Drawable icon, Canvas destination, float horizontalOffset) {
        int canvasState = destination.save();
        destination.translate(3.0F + horizontalOffset, this.icons_center - (float)(icon.getIntrinsicHeight() / 2));
        icon.draw(destination);
        destination.restoreToCount(canvasState);
    }

    private void drawIconInverse(Drawable iconDrawable, Canvas canvasInstance, float iconXOffset) {
        int canvasState = canvasInstance.save();
        canvasInstance.translate((float)(this.available_width - 3) - iconXOffset - (float)iconDrawable.getBounds().width(), this.icons_center - (float)(iconDrawable.getBounds().height() / 2));
        iconDrawable.draw(canvasInstance);
        canvasInstance.restoreToCount(canvasState);
    }

    private void drawText(Canvas targetCanvas, int textColor) {
        if (this.mStatusDrawer != null) {
            this.mStatusDrawer.setTextColor(textColor);
            if (PreferenceTable.use_contactlist_items_shadow) {
                this.mStatusDrawer.setShadowLayer(1.0F, 1.0F, 1.0F, 0xFF000000);
            } else {
                this.mStatusDrawer.setShadowLayer(0.0F, 0.0F, 0.0F, 0);
            }

            this.mStatusDrawer.draw(targetCanvas);
        }

    }

    private void updateAvatarPath() {
        int size = (int)(AVATAR_SIZE * resources.dm.density);
        RectF rect = this.avatar_bounds != null
                ? new RectF(this.avatar_bounds)
                : new RectF(0f, 0f, size, size);
        this.avatar_path.reset();
        if (PreferenceTable.ms_round_avatars) {
            float cx = rect.centerX();
            float cy = rect.centerY();
            float radius = Math.min(rect.width(), rect.height()) / 2f;
            this.avatar_path.addCircle(cx, cy, radius, Direction.CCW);
        } else {
            this.avatar_path.addRoundRect(rect, 5f, 5f, Direction.CCW);
        }
    }

    private float getCounterHeight() {
        return Math.abs(this.counter_.getTextSize() + this.counter_.descent());
    }

    private float getCounterWidth() {
        return Math.abs(this.counter_.measureText(String.valueOf(this.unreaded_count)));
    }

    private int getVCenter() {
        int groupIconHeight = 0;
        int statusIconHeight = 0;
        int statusHeight = 0;
        int exStatusIconHeight = (byte) 0;
        if (this.type != 4) {
            int groupOpenedHeight = resources.group_opened.getIntrinsicHeight();
            if (this.status != null) {
                statusHeight = this.status.getIntrinsicHeight();
            }

            groupIconHeight = groupOpenedHeight;
            statusIconHeight = statusHeight;
            if (this.ex_status != null) {
                exStatusIconHeight = this.ex_status.getIntrinsicHeight();
            }
        }

        return this.max(groupIconHeight, statusIconHeight, exStatusIconHeight, (int)(this.name_.getTextSize() + 6.0F)) / 2;
    }

    private int max(int var1, int var2, int var3, int var4) {
        return Math.max(var1, Math.max(var2, Math.max(var3, var4)));
    }

    private void measureHeight(int var1) {
        float totalFontHeight = (float)(-this.name_.getFontMetricsInt().ascent - this.name_.getFontMetricsInt().descent);
        this.lbl_left = 3.0F;
        this.icons_center = (float)(this.getVCenter() + 3);
        this.lbl_top = this.icons_center + totalFontHeight / 2.0F;
        this.sts_top = this.lbl_top + 3.0F + 3.0F;
        switch (this.type) {
            case 0:
                this.lbl_left += (float)(this.status.getIntrinsicWidth() + 3);
                if (this.ex_status != null) {
                    this.lbl_left += (float)this.ex_status.getIntrinsicWidth();
                }

                if (this.draw_avatar) {
                    totalFontHeight = (float)this.avatar_back.getBounds().width();
                    this.lbl_left = this.lbl_left + totalFontHeight;
                    this.av_h = totalFontHeight;
                }
                break;
            case 1:
            case 4:
                this.lbl_left += (float)resources.group_opened.getIntrinsicWidth();
                this.lbl_left += 6.0F;
                this.lbl_left += ((float)(this.available_width - 6) - this.lbl_left) / 2.0F - this.name_.measureText(this.name) / 2.0F;
                break;
            case 2:
                this.lbl_left += (float)resources.group_opened.getIntrinsicWidth();
                if (this.status != null) {
                    this.lbl_left += (float)this.status.getIntrinsicWidth();
                }

                if (this.ex_status != null) {
                    this.lbl_left += (float)this.ex_status.getIntrinsicWidth();
                }

                this.lbl_left += 6.0F;
                this.lbl_left += ((float)(this.available_width - 6) - this.lbl_left) / 2.0F - this.name_.measureText(this.name) / 2.0F;
            case 3:
        }

        this.name = TextUtils.ellipsize(this.name, this.name_, this.calculateNameAvail(), TruncateAt.END).toString();
        this.sts_left = this.lbl_left + 3.0F;
        float var3 = 0.0F;
        totalFontHeight = var3;
        if (this.draw_status_desc) {
            totalFontHeight = var3;
            if (this.status_text != null) {
                totalFontHeight = var3;
                if (!this.status_text.isEmpty()) {
                    totalFontHeight = (float)this.measureText(this.status_text, (int)this.status_text_.getTextSize(), (int)((float)this.available_width - this.sts_left));
                }
            }
        }

        totalFontHeight = Math.max(this.av_h, this.icons_center * 2.0F - 6.0F + totalFontHeight);
        this.setMeasuredDimension(var1, (int)((float)0 + totalFontHeight + 6.0F));
    }

    @SuppressLint("Range")
    private int measureText(String textToMeasure, int textSizeInSp, int specMode) {
        this.mStatusDrawer = new TextView(this.getContext());
        this.mStatusDrawer.setDrawingCacheEnabled(false);
        this.mStatusDrawer.setWillNotCacheDrawing(true);
        this.mStatusDrawer.setMaxLines(5);
        this.mStatusDrawer.setEllipsize(TruncateAt.END);
        this.mStatusDrawer.setText(textToMeasure);
        this.mStatusDrawer.setTextSize((float)textSizeInSp);
        textSizeInSp = MeasureSpec.makeMeasureSpec(1_073_741_824, specMode);
        specMode = MeasureSpec.makeMeasureSpec(Integer.MIN_VALUE, MeasureSpec.UNSPECIFIED);
        this.mStatusDrawer.measure(textSizeInSp, specMode);
        this.mStatusDrawer.layout(0, 0, this.mStatusDrawer.getMeasuredWidth(), this.mStatusDrawer.getMeasuredHeight());
        return this.mStatusDrawer.getMeasuredHeight();
    }

    private void prepareModulator() {
        if (this.has_unreaded) {
            this.value_ += this.direction_;
            if (this.value_ > 255) {
                this.value_ = 255;
                this.direction_ = MODULATOR_SPEED * -1;
            }

            if (this.value_ < 0) {
                this.value_ = 0;
                this.direction_ = MODULATOR_SPEED;
            }

            this.effect.setAlpha(this.value_);
            this.invalidate();
        }

    }

    /** @noinspection unused*/
    private int proceedBlinkColor(long currentTime, long blinkStartTime, int currentColor) {
        if ((int)(Math.abs(currentTime - blinkStartTime) / BLINK_INTERVAL_MILLIS) % 2 == 0) {
            currentColor = 0;
        }

        return currentColor;
    }

    private void reset() {
        int avatarSize = (int)(40.0F * resources.dm.density);
        Rect avatarPadding = new Rect();
        this.avatar_back.getPadding(avatarPadding);
        this.avatar_bounds = new Rect(avatarPadding.left, avatarPadding.top, avatarSize - avatarPadding.right, avatarSize - avatarPadding.bottom);
        updateAvatarPath();
        this.draw_avatar = false;
        this.online = 0;
        this.total = 0;
        this.type = -1;
        this.typing = false;
        this.ex_status = null;
        this.visibility = null;
        this.ignore = null;
        this.draw_client = false;
        this.draw_status_desc = false;
        this.has_unreaded = false;
        this.use_blink = false;
        this.unreaded_count = 0;
        this.status_text = null;
        this.av_h = 0.0F;
        this.lbl_top = 0.0F;
        this.sts_top = 0.0F;
        this.lbl_left = 0.0F;
        this.sts_left = 0.0F;
    }

    public final void dispatchDraw(Canvas drawTarget) {
        this.prepareModulator();
        if (this.name != null) {
            int nameColor = 0;
            if (this.use_blink) {
                nameColor = this.name_.getColor();
                if (this.blink_visible) {
                    this.name_.setColor(nameColor);
                } else {
                    this.name_.setColor(0);
                }
            }

            if (this.has_unreaded) {
                drawTarget.drawText(this.name, this.lbl_left, this.lbl_top, this.effect);
            }

            drawTarget.drawText(this.name, this.lbl_left, this.lbl_top, this.name_);
            if (this.use_blink) {
                this.name_.setColor(nameColor);
            }
        }

        switch (this.type) {
            case 0:
                int saveCount = 0;
                if (this.draw_avatar) {
                    saveCount = drawTarget.save();
                    drawTarget.translate(3.0F, 3.0F);
                    if (!PreferenceTable.ms_round_avatars) {
                        this.avatar_back.draw(drawTarget);
                    }
                    if (PreferenceTable.ms_round_avatars) {
                        int clip = drawTarget.save();
                        drawTarget.clipPath(this.avatar_path);
                        this.avatar.draw(drawTarget);
                        drawTarget.restoreToCount(clip);
                    } else {
                        this.avatar.draw(drawTarget);
                    }
                    drawTarget.restoreToCount(saveCount);
                    saveCount = this.avatar_back.getBounds().width();
                }

                if (this.has_unreaded) {
                    if (this.typing) {
                        this.status.setAlpha(255);
                    } else {
                        this.status.setAlpha(this.value_);
                    }

                    this.drawIcon(this.status, drawTarget, (float)saveCount);
                    ((BitmapDrawable)this.status).setCustomPaint(this.effect);
                    this.drawIcon(this.status, drawTarget, (float)saveCount);
                    ((BitmapDrawable)this.status).setCustomPaint(null);
                } else {
                    this.drawIcon(this.status, drawTarget, (float)saveCount);
                }

                int horizontalOffset = saveCount + this.status.getIntrinsicWidth();
                saveCount = horizontalOffset;
                if (this.ex_status != null) {
                    this.drawIcon(this.ex_status, drawTarget, (float)horizontalOffset);
                    if (this.has_unreaded) {
                        ((BitmapDrawable)this.ex_status).setCustomPaint(this.effect);
                        this.drawIcon(this.ex_status, drawTarget, (float)horizontalOffset);
                        ((BitmapDrawable)this.ex_status).setCustomPaint(null);
                    }

                    saveCount = horizontalOffset + this.ex_status.getIntrinsicWidth();
                }

                if (this.unreaded_count > 1) {
                    float counterWidthInPixels = this.getCounterWidth();
                    float counterHeightInPixels = this.getCounterHeight();
                    horizontalOffset = drawTarget.save();
                    float iconCenterY = this.icons_center;
                    float iconHeightInPixels = (float)(this.status.getIntrinsicHeight() / 2);
                    drawTarget.translate((float)saveCount - counterWidthInPixels, iconCenterY + iconHeightInPixels - counterHeightInPixels);
                    Drawable unreadMessageBackground = resources.msgs_number_back;
                    unreadMessageBackground.setBounds(-3, -3, (int)counterWidthInPixels + 3, (int)this.counter_.getTextSize() + 3);
                    unreadMessageBackground.draw(drawTarget);
                    drawTarget.drawText(String.valueOf(this.unreaded_count), 0.0F, -this.counter_.ascent(), this.counter_);
                    drawTarget.restoreToCount(horizontalOffset);
                }

                horizontalOffset = 0;
                saveCount = horizontalOffset;
                if (this.draw_client) {
                    saveCount = horizontalOffset;
                    if (this.client != null) {
                        this.drawIconInverse(this.client, drawTarget, (float)0);
                        saveCount = this.client.getBounds().width();
                    }
                }

                horizontalOffset = saveCount;
                if (this.visibility != null) {
                    this.drawIconInverse(this.visibility, drawTarget, (float)saveCount);
                    horizontalOffset = saveCount + this.visibility.getIntrinsicWidth();
                }

                if (this.ignore != null) {
                    this.drawIconInverse(this.ignore, drawTarget, (float)horizontalOffset);
                    this.ignore.getIntrinsicWidth();
                }

                if (this.draw_status_desc && this.status_text != null) {
                    horizontalOffset = drawTarget.save();
                    drawTarget.translate(this.sts_left, this.sts_top);
                    saveCount = 0;
                    if (this.use_blink) {
                        saveCount = this.status_text_.getColor();
                        if (this.blink_visible) {
                            this.status_text_.setColor(saveCount);
                        } else {
                            this.status_text_.setColor(0);
                        }
                    }

                    this.drawText(drawTarget, this.status_text_.getColor());
                    if (this.use_blink) {
                        this.status_text_.setColor(saveCount);
                    }

                    drawTarget.restoreToCount(horizontalOffset);
                }
                break;
            case 1:
                if (this.opened) {
                    this.drawIcon(resources.group_opened, drawTarget, (float)0);
                } else {
                    this.drawIcon(resources.group_closed, drawTarget, (float)0);
                }

                resources.group_opened.getIntrinsicWidth();
                break;
            case 2:
                if (this.opened) {
                    this.drawIcon(resources.group_opened, drawTarget, (float)0);
                } else {
                    this.drawIcon(resources.group_closed, drawTarget, (float)0);
                }

                int groupIconWidth = resources.group_opened.getIntrinsicWidth();
                int horizontalPosition = groupIconWidth;
                if (this.status != null) {
                    this.drawIcon(this.status, drawTarget, (float)(groupIconWidth + 3));
                    horizontalPosition = groupIconWidth + this.status.getIntrinsicWidth();
                }

                if (this.ex_status != null) {
                    this.drawIcon(this.ex_status, drawTarget, (float)(horizontalPosition + 3));
                    this.ex_status.getIntrinsicWidth();
                }
        }

        if (this.use_blink && !this.blink_posted) {
            this.postDelayed(new Runnable() {
                public void run() {
                    boolean isVisible = false;
                    RosterItemView.this.blink_posted = false;
                    RosterItemView var2 = RosterItemView.this;
                    if (!RosterItemView.this.blink_visible) {
                        isVisible = true;
                    }

                    var2.blink_visible = isVisible;
                    RosterItemView.this.invalidate();
                    RosterItemView.this.checkNeedBlinking();
                }
            }, 468L);
            this.blink_posted = true;
        }

    }

    public final void onMeasure(int newWidthMeasureSpec, int heightSpec) {
        newWidthMeasureSpec = MeasureSpec.getSize(newWidthMeasureSpec);
        this.available_width = newWidthMeasureSpec;
        this.measureHeight(newWidthMeasureSpec);
    }

    public final void update(ContactlistItem item) {
        reset();
        this.name = item.name;
        this.name_.setColor(-1);
        this.name_.setTextSize(PreferenceTable.clTextSize * resources.dm.density);
        this.status_text_.setTextSize((this.name_.getTextSize() * 0.7f) / resources.dm.density);
        this.counter_.setTextSize(this.name_.getTextSize() * 0.8f);
        this.counter_.setColor(-1);
        this.counter_.setShadowLayer(1.0f, 0.0f, 0.0f, 0xFF000000);
        this.effect.setColor(-1);
        this.effect.setTextSize(this.name_.getTextSize());
        if (!PreferenceTable.use_contactlist_items_shadow) {
            this.name_.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
        } else {
            this.name_.setShadowLayer(1.0f, 1.0f, 1.0f, 0xFF000000);
        }
        this.blink_zero_ts = item.presense_timestamp;
        checkNeedBlinking();
        switch (item.itemType) {
            case ContactlistItem.CONTACT:
                setBackgroundColor(0);
                resources.attachContactlistItemBack(this);
                ICQContact contact = (ICQContact) item;
                this.type = 0;
                if (contact.typing) {
                    this.name_.setColor(ColorScheme.getColor(31));
                } else if (contact.isChating) {
                    this.name_.setColor(ColorScheme.getColor(27));
                } else if (!contact.added || !contact.authorized) {
                    this.name_.setColor(ColorScheme.getColor(29));
                } else if (contact.status == IMProfile.STATUS_OFFLINE) {
                    this.name_.setColor(ColorScheme.getColor(30));
                } else {
                    this.name_.setColor(ColorScheme.getColor(28));
                }
                if (PreferenceTable.s_ms_show_xstatuses) {
                    this.ex_status = contact.xstatus;
                }
                if (PreferenceTable.s_ms_show_clients && contact.client.info_index != -1) {
                    this.client = contact.client.icon;
                    if (this.client != null && this.client.getBounds().isEmpty()) {
                        this.client.setBounds(0, 0, this.client.getIntrinsicWidth(), this.client.getIntrinsicHeight());
                    }
                    this.draw_client = true;
                }
                if (!contact.authorized) {
                    this.client = resources.unauthorized_icon;
                    this.draw_client = true;
                }
                if (contact.isVisible()) {
                    this.visibility = resources.visible;
                }
                if (contact.isInvisible()) {
                    this.visibility = resources.invisible;
                }
                if (contact.isIgnore()) {
                    this.ignore = resources.ignore;
                }
                if (PreferenceTable.ms_show_avatars) {
                    if (contact.avatar != null) {
                        this.avatar = contact.avatar;
                    } else {
                        this.avatar = resources.ctx.getResources().getDrawable(R.drawable.no_avatar);
                    }
                    computeBounds(this.avatar, this.avatar_bounds);
                    this.avatar.setBounds(this.avatar_bounds);
                    this.draw_avatar = true;
                } else {
                    this.draw_avatar = false;
                }
                this.has_unreaded = contact.hasUnreadMessages;
                if (contact.typing) {
                    this.status = resources.typing;
                    this.typing = true;
                } else {
                    if (contact.added) {
                        if (contact.authorized) {
                            if (utilities.isUIN(contact.ID)) {
                                this.status = resources.getStatusIcon(contact.status);
                            } else {
                                this.status = resources.getMrimStatusIcon(contact.status);
                            }
                        } else {
                            this.status = resources.unauthorized;
                        }
                    } else {
                        this.status = resources.not_added;
                    }
                    if (contact.hasUnreadMessages) {
                        if (contact.hasUnreadedAuthRequest) {
                            this.status = resources.unauthorized_icon;
                        } else if (contact.hasUnreadedFileRequest) {
                            this.status = resources.file;
                        } else {
                            this.status = resources.msg_in_blink;
                        }
                    }
                }
                this.unreaded_count = contact.getUnreadCount();
                if (PreferenceTable.show_away_in_cl) {
                    if (!utilities.isEmptyForDisplay(contact.xtraz_text)) {
                        this.status_text = contact.xtraz_text;
                        this.draw_status_desc = true;
                        break;
                    } else if (!utilities.isEmptyForDisplay(contact.away_status)) {
                        this.status_text = contact.away_status;
                        this.draw_status_desc = true;
                        break;
                    }
                }
                break;
            case ContactlistItem.GROUP:
                setBackgroundColor(ColorScheme.getColor(33));
                resources.attachContactlistGroupItemBack(this);
                this.name_.setColor(ColorScheme.getColor(34));
                ICQGroup group = (ICQGroup) item;
                this.opened = group.opened;
                this.online = group.online;
                this.total = group.total;
                this.name = this.name + "(" + this.online + "/" + this.total + ")";
                this.type = 1;
                this.draw_avatar = false;
                break;
            case ContactlistItem.PROFILE_GROUP:
                this.name_.setColor(ColorScheme.getColor(36));
                setBackgroundColor(ColorScheme.getColor(35));
                resources.attachContactlistGroupItemBack(this);
                ICQGroup group2 = (ICQGroup) item;
                if (group2.profile.connected) {
                    if (group2.profile.qip_status != null) {
                        this.status = qip_statuses.getIcon(group2.profile.qip_status);
                    } else {
                        this.status = resources.getStatusIcon(group2.profile.status);
                    }
                } else if (group2.profile.connecting) {
                    this.status = resources.connecting;
                } else {
                    this.status = resources.offline;
                }
                if (group2.profile.xsts != -1) {
                    this.ex_status = xstatus.icons[group2.profile.xsts];
                }
                this.opened = group2.profile.openedInContactList;
                this.online = group2.online;
                this.total = group2.total;
                this.name = this.name + "(" + this.online + "/" + this.total + ")";
                this.type = 2;
                this.draw_avatar = false;
                break;
            case 4:
                setBackgroundColor(0);
                resources.attachContactlistItemBack(this);
                JContact jcontact = (JContact) item;
                this.type = 0;
                if (jcontact.typing) {
                    this.name_.setColor(ColorScheme.getColor(31));
                } else if (jcontact.isChating) {
                    this.name_.setColor(ColorScheme.getColor(27));
                } else if (!jcontact.isOnline()) {
                    this.name_.setColor(ColorScheme.getColor(30));
                } else {
                    this.name_.setColor(ColorScheme.getColor(28));
                }
                this.ex_status = jcontact.ext_status;
                if (PreferenceTable.s_ms_show_clients) {
                    Drawable ic_client = jcontact.getClient();
                    if (ic_client != null) {
                        this.client = ic_client;
                        if (this.client.getBounds().isEmpty()) {
                            this.client.setBounds(0, 0, this.client.getIntrinsicWidth(), this.client.getIntrinsicHeight());
                        }
                        this.draw_client = true;
                    }
                }
                if (jcontact.subscription != 2 && jcontact.subscription != 3 && !jcontact.conf_pm) {
                    this.client = resources.unauthorized_icon;
                    this.draw_client = true;
                }
                if (PreferenceTable.ms_show_avatars) {
                    if (jcontact.avatar != null) {
                        this.avatar = jcontact.avatar;
                    } else {
                        this.avatar = resources.ctx.getResources().getDrawable(R.drawable.no_avatar);
                    }
                    computeBounds(this.avatar, this.avatar_bounds);
                    this.avatar.setBounds(this.avatar_bounds);
                    this.draw_avatar = true;
                } else {
                    this.draw_avatar = false;
                }
                this.has_unreaded = jcontact.hasUnreadMessages;
                if (jcontact.typing) {
                    this.status = resources.typing;
                    this.typing = true;
                } else {
                    switch (jcontact.profile.type) {
                        case 0:
                            if (jcontact.conf_pm) {
                                this.status = resources.jabber_conf_pm;
                                break;
                            } else if (jcontact.isOnline()) {
                                switch (jcontact.getStatus()) {
                                    case 0:
                                        this.status = resources.jabber_chat;
                                        break;
                                    case 1:
                                        this.status = resources.jabber_online;
                                        break;
                                    case 2:
                                        this.status = resources.jabber_away;
                                        break;
                                    case 3:
                                        this.status = resources.jabber_dnd;
                                        break;
                                    case 4:
                                        this.status = resources.jabber_na;
                                        break;
                                }
                            } else {
                                this.status = resources.jabber_offline;
                                break;
                            }
                            break;
                        case 1:
                            if (jcontact.isOnline()) {
                                this.status = resources.vk_online;
                            } else {
                                this.status = resources.vk_offline;
                            }
                            break;
                        case 2:
                            if (jcontact.isOnline()) {
                                this.status = resources.yandex_online;
                            } else {
                                this.status = resources.yandex_offline;
                            }
                            break;
                        case 3:
                            if (jcontact.isOnline()) {
                                this.status = resources.gtalk_online;
                            } else {
                                this.status = resources.gtalk_offline;
                            }
                            break;
                        case 4:
                            if (jcontact.isOnline()) {
                                //noinspection UnusedAssignment
                                this.status = resources.qip_online;
                            } else {
                                this.status = resources.qip_offline;
                            }
                            break;
                    }
                    if (jcontact.hasUnreadMessages) {
                        this.status = resources.msg_in_blink;
                    }
                }
                this.unreaded_count = jcontact.getUnreadCount();
                if (PreferenceTable.show_away_in_cl || jcontact.conf_pm) {
                    String desc = jcontact.getStatusDescription();
                    if (!utilities.isEmptyForDisplay(desc)) {
                        this.status_text = desc;
                        this.draw_status_desc = true;
                        break;
                    }
                }
                break;
            case 5:
                this.name_.setColor(ColorScheme.getColor(36));
                setBackgroundColor(ColorScheme.getColor(35));
                resources.attachContactlistGroupItemBack(this);
                ICQGroup group3 = (ICQGroup) item;
                switch (group3.jprofile.type) {
                    case 0:
                        if (group3.jprofile.connected) {
                            switch (group3.jprofile.status) {
                                case 0:
                                    this.status = resources.jabber_chat;
                                    break;
                                case 1:
                                    this.status = resources.jabber_online;
                                    break;
                                case 2:
                                    this.status = resources.jabber_away;
                                    break;
                                case 3:
                                    this.status = resources.jabber_dnd;
                                    break;
                                case 4:
                                    this.status = resources.jabber_na;
                                    break;
                            }
                        } else {
                            this.status = resources.jabber_offline;
                            break;
                        }
                        break;
                    case 1:
                        if (group3.jprofile.connected) {
                            this.status = resources.vk_online;
                        } else {
                            this.status = resources.vk_offline;
                        }
                        break;
                    case 2:
                        if (group3.jprofile.connected) {
                            this.status = resources.yandex_online;
                        } else {
                            this.status = resources.yandex_offline;
                        }
                        break;
                    case 3:
                        if (group3.jprofile.connected) {
                            this.status = resources.gtalk_online;
                        } else {
                            this.status = resources.gtalk_offline;
                        }
                        break;
                    case 4:
                        if (group3.jprofile.connected) {
                            this.status = resources.qip_online;
                        } else {
                            this.status = resources.qip_offline;
                        }
                        break;
                }
                this.opened = group3.jprofile.openedInContactList;
                this.online = group3.online;
                this.total = group3.total;
                this.name = this.name + "(" + this.online + "/" + this.total + ")";
                this.type = 2;
                this.draw_avatar = false;
                break;
            case 6:
                setBackgroundColor(ColorScheme.getColor(33));
                resources.attachContactlistGroupItemBack(this);
                this.name_.setColor(ColorScheme.getColor(34));
                JGroup jgroup = (JGroup) item;
                this.opened = jgroup.opened;
                this.online = jgroup.online;
                this.total = jgroup.total;
                this.name = this.name + "(" + this.online + "/" + this.total + ")";
                this.type = 1;
                this.draw_avatar = false;
                break;
            case 7:
                setBackgroundColor(0);
                resources.attachContactlistItemBack(this);
                this.type = 0;
                MMPContact mmp_contact = (MMPContact) item;
                if (mmp_contact.typing) {
                    this.name_.setColor(ColorScheme.getColor(31));
                } else if (mmp_contact.isChating) {
                    this.name_.setColor(ColorScheme.getColor(27));
                } else if (mmp_contact.status > 0) {
                    this.name_.setColor(ColorScheme.getColor(28));
                } else {
                    this.name_.setColor(ColorScheme.getColor(30));
                }
                if (PreferenceTable.ms_show_avatars) {
                    if (mmp_contact.avatar != null) {
                        this.avatar = mmp_contact.avatar;
                    } else {
                        this.avatar = resources.ctx.getResources().getDrawable(R.drawable.no_avatar);
                    }
                    computeBounds(this.avatar, this.avatar_bounds);
                    this.avatar.setBounds(this.avatar_bounds);
                    this.draw_avatar = true;
                } else {
                    this.draw_avatar = false;
                }
                this.has_unreaded = mmp_contact.hasUnreadMessages;
                if (mmp_contact.typing) {
                    this.status = resources.typing;
                    this.typing = true;
                } else {
                    switch (mmp_contact.status) {
                        case 0:
                            this.status = resources.mrim_offline;
                            break;
                        case 2:
                            this.status = resources.mrim_away;
                            break;
                        case 5:
                            this.status = resources.mrim_dnd;
                            break;
                        case 6:
                            this.status = resources.mrim_oc;
                            break;
                        case 7:
                            this.status = resources.mrim_na;
                            break;
                        case 8:
                            this.status = resources.mrim_lunch;
                            break;
                        case 9:
                            this.status = resources.mrim_work;
                            break;
                        case 10:
                            this.status = resources.mrim_home;
                            break;
                        case 11:
                            this.status = resources.mrim_depress;
                            break;
                        case 12:
                            this.status = resources.mrim_angry;
                            break;
                        case 13:
                            this.status = resources.mrim_chat;
                            break;
                        default: // 1 - 4
                            this.status = resources.mrim_online;
                            break;
                    }
                    if (mmp_contact.hasUnreadMessages) {
                        this.status = resources.msg_in_blink;
                    }
                }
                this.unreaded_count = mmp_contact.getUnreadCount();
                if (PreferenceTable.show_away_in_cl && !utilities.isEmptyForDisplay(mmp_contact.status_text)) {
                    this.status_text = mmp_contact.status_text;
                    this.draw_status_desc = true;
                    break;
                }
                break;
            case 8:
                this.name_.setColor(ColorScheme.getColor(36));
                setBackgroundColor(ColorScheme.getColor(35));
                resources.attachContactlistGroupItemBack(this);
                MMPGroup mmpgroup = (MMPGroup) item;
                if (mmpgroup.profile.connected) {
                    switch (mmpgroup.profile.getTranslatedStatus()) {
                        case 0:
                            this.status = resources.mrim_offline;
                            break;
                        case 2:
                            this.status = resources.mrim_away;
                            break;
                        case 5:
                            this.status = resources.mrim_dnd;
                            break;
                        case 6:
                            this.status = resources.mrim_oc;
                            break;
                        case 7:
                            this.status = resources.mrim_na;
                            break;
                        case 8:
                            this.status = resources.mrim_lunch;
                            break;
                        case 9:
                            this.status = resources.mrim_work;
                            break;
                        case 10:
                            this.status = resources.mrim_home;
                            break;
                        case 11:
                            this.status = resources.mrim_depress;
                            break;
                        case 12:
                            this.status = resources.mrim_angry;
                            break;
                        case 13:
                            this.status = resources.mrim_chat;
                            break;
                        default: // 1 - 4
                            this.status = resources.mrim_online;
                            break;
                    }
                } else {
                    this.status = resources.mrim_offline;
                }
                this.opened = mmpgroup.profile.openedInContactList;
                this.online = mmpgroup.online;
                this.total = mmpgroup.total;
                this.name = this.name + "(" + this.online + "/" + this.total + ")";
                this.type = 2;
                this.draw_avatar = false;
                break;
            case 9:
                setBackgroundColor(ColorScheme.getColor(33));
                resources.attachContactlistGroupItemBack(this);
                this.name_.setColor(ColorScheme.getColor(34));
                MMPGroup mmpgroup2 = (MMPGroup) item;
                this.opened = mmpgroup2.opened;
                this.online = mmpgroup2.online;
                this.total = mmpgroup2.total;
                this.name = this.name + "(" + this.online + "/" + this.total + ")";
                this.type = 1;
                this.draw_avatar = false;
                break;
            case 10:
                setBackgroundColor(0);
                resources.attachContactlistItemBack(this);
                this.type = 0;
                ConferenceItem conference_item = (ConferenceItem) item;
                if (conference_item.hasUnreadMessages) {
                    this.has_unreaded = true;
                    this.status = resources.msg_in_blink;
                } else {
                    this.has_unreaded = false;
                }
                Conference conference = conference_item.conference;
                this.name = JProtocol.getNameFromFullID(conference.JID);
                if (conference.isOnline()) {
                    this.name_.setColor(ColorScheme.getColor(28));
                    this.status = resources.jabber_conference;
                } else {
                    this.name_.setColor(ColorScheme.getColor(30));
                    this.status = resources.jabber_conference_offline;
                }
                if (conference_item.hasUnreadMessages) {
                    this.has_unreaded = true;
                    this.status = resources.msg_in_blink;
                } else {
                    this.has_unreaded = false;
                }
                if (conference.isOnline()) {
                    this.status_text = utilities.match(resources.getString("s_conference_information"), new String[]{String.valueOf(conference.users.size()), String.valueOf(conference.unreaded)});
                }
                this.draw_status_desc = true;
                this.unreaded_count = conference_item.getUnreadCount();
                break;
            case 11:
                this.name_.setTextSize(PreferenceTable.clTextSize * resources.dm.density * 0.7f);
                this.name_.setColor(ColorScheme.getColor(36));
                setBackgroundColor(ColorScheme.getColor(35));
                resources.attachContactlistGroupItemBack(this);
                this.type = 4;
                break;
        }
        this.status_text_.setColor(ColorScheme.getColor(45));
    }
}