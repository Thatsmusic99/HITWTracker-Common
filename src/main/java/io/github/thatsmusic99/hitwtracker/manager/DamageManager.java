package io.github.thatsmusic99.hitwtracker.manager;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.util.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public abstract class DamageManager<D, S> implements CobwebTracking, DamageTracking<D>, HotPotatoTracking, StatusTracking<S>, VelocityTracking {

    private static final Logger LOGGER = LoggerFactory.getLogger(DamageManager.class);
    protected long lastInAir = System.currentTimeMillis() / 50;
    protected @Nullable D damage;
    protected @Nullable VelocityStatus velocity;
    protected @Nullable S effect;
    protected @Nullable Timer cobwebTimer;
    protected boolean hotPotatoTracker = false;
    protected boolean explosion = false;
    protected boolean launched = false;
    protected boolean onGround = false;
    protected byte count = 0;

    @Override
    public void onDamage(D damageSource) {

        if (!GameTracker.isTracking()) return;
        if (damageSource == null) return;
        this.damage = damageSource;
        this.count = 1;
        this.lastInAir = System.currentTimeMillis() / 50;

    }

    @Override
    public @Nullable D gameTracker$getLastDamageSource() {
        return this.damage;
    }

    @Override
    public void gameTracker$onGround(boolean onGround) {
        boolean changedGround = onGround ^ this.onGround;
        this.onGround = onGround;
        if (!onGround) this.lastInAir = System.currentTimeMillis() / 50;
        if (!GameTracker.isTracking()) return;
        gameTracker$checkSandfall();
        if (this.damage == null && this.velocity == null) return;

        boolean onGroundTooLong = System.currentTimeMillis() / 50 - this.lastInAir > 60;
        if (onGroundTooLong) {
            gameTracker$flush();
            LOGGER.info("Tracking wiped (ground)");
            return;
        }

        if (changedGround && onGround) {
            if (this.count == 0) {
                gameTracker$flush();
                LOGGER.info("Tracking wiped");
            } else {
                this.count = 0;
                LOGGER.info("Tracking put down");
            }
        }
    }

    @Override
    public void gameTracker$onSandfall() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.SANDFALL;
            this.lastInAir = System.currentTimeMillis() / 50;
            this.count = 1;
        }
    }

    @Override
    public @Nullable VelocityStatus gameTracker$getVelocityStatus() {
        return velocity;
    }

    @Override
    public void gameTracker$flush() {
        this.damage = null;
        this.velocity = null;
        this.count = -1;
        if (this.effect != null && gameTracker$getAmplifier(this.effect) == 0) this.effect = null;
    }

    @Override
    public void gameTracker$onPotatoWarning() {
        this.hotPotatoTracker = true;
    }

    @Override
    public void gameTracker$coolPotatoWarning() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                hotPotatoTracker = false;
            }
        }, 1000);
    }

    @Override
    public boolean gameTracker$hasPotatoWarning() {
        return hotPotatoTracker;
    }

    @Override
    public void gameTracker$onExplosion() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.velocity = VelocityStatus.HOT_POTATO;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            LOGGER.info("Explosion tracked");
        }
    }

    @Override
    public void gameTracker$onExplosionSound() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.explosion = true;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            if (!this.launched) return;
            this.velocity = VelocityStatus.HOT_POTATO;
        }
    }

    @Override
    public void gameTracker$onExplosionLaunch() {
        if (GameTracker.isTracking() && this.hotPotatoTracker) {
            this.launched = true;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            if (!this.explosion) return;
            this.velocity = VelocityStatus.HOT_POTATO;
        }
    }

    @Override
    public void onRodPull() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.FISHING_RODS;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            LOGGER.info("Rod pull tracked");
        }
    }

    @Override
    public void gameTracker$onBlastOff() {
        if (GameTracker.isTracking()) {
            this.velocity = VelocityStatus.BLAST_OFF;
            this.count = 1;
            this.lastInAir = System.currentTimeMillis() / 50;
            LOGGER.info("Blast-off tracked");
        }
    }

    @Override
    public void onWeb() {
        if (!GameTracker.isTracking()) return;
        if (cobwebTimer == null) {
            this.velocity = VelocityStatus.STICKY_SHOES;
        } else {
            this.velocity = VelocityStatus.COBWEBS;
        }
        this.count = 1;
        this.lastInAir = System.currentTimeMillis() / 50;
    }

    @Override
    public void gameTracker$onCobwebProvide() {
        cobwebTimer = new Timer();
        cobwebTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cobwebTimer = null;
            }
        }, 20000);
    }

    @Override
    public void gameTracker$applyStatusEffect(@NotNull S effect) {
        this.effect = effect;
    }
}
