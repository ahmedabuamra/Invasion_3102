package com.oop.platformer.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.oop.platformer.GameClass;
import com.oop.platformer.GameObjects.*;
import com.oop.platformer.Scenes.Hud;
import com.oop.platformer.Screens.Level1;

import static com.oop.platformer.Constants.FIRE_RATE;

public class LevelManager {

    GameClass gameClass;

    private Level1 level1Screen;
    private World world;

    //Game Objects
    private Player player;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;
    private Enemy bossEnemy;

    //Game hud
    private Hud hud;
    private OrthographicCamera gameCam;

    private boolean isDeathSoundPlayed = false;
    private float shootTimer;

    public LevelManager(GameClass gameClass, Level1 level1Screen, Player player, Array<Enemy> enemies, Hud hud, World world, Array<Bullet> bullets, OrthographicCamera gameCam) {
        this.gameClass = gameClass;
        this.level1Screen = level1Screen;
        this.player = player;
        this.enemies = enemies;
        this.hud = hud;
        this.world = world;
        this.bullets = bullets;
        this.gameCam = gameCam;
        shootTimer = 0;

        //finding the boss enemy in the enemies array
        for (Enemy enemy : enemies) {
            if (enemy instanceof BossEnemy) {
                this.bossEnemy = enemy;
                break;
            }
        }
    }

    public void update(float deltaTime) {

        if (player.isDead()) {
            GameClass.isMusicPaused = true;
            if (!isDeathSoundPlayed) {
                Assets.instance.audio.playerDied.play();
                isDeathSoundPlayed = true;
            }
            if (player.lostLevel()) {
                System.out.println("Level is lost");
                gameOver(false);
            }
        } else if (bossEnemy.destroyed) {
            if (!player.win) {
                player.setWin();
            } else if (player.wonLevel()) {
                System.out.println("you won");
                gameOver(true);
            }
        } else
            handlePlayerInput(deltaTime);
        checkBulletsPosition();
        hud.setLives(player.getLives());
        hud.setScore(player.getScore());
    }

    // returns a bullet to be added to bullets ArrayList in level1 screen
    //NOTE*** +0.06f to adjust the spritePosition of the bullet exit to the barrel
    public Bullet spawnBullet() {
        Assets.instance.audio.gunShotSound.play();
        if (player.isRunningRight()) {
            return new Bullet(world, new Vector2(player.body.getPosition().x + 2 / GameClass.PPM + 0.20f, player.body.getPosition().y + 0.08f),
                    true);
        } else {
            return new Bullet(world, new Vector2(player.body.getPosition().x + 2 / GameClass.PPM - 0.20f, player.body.getPosition().y + 0.08f),
                    false);
        }

    }

    public void playerIsHit() {
        player.hitPlayer();
    }

    private void handlePlayerInput(float deltaTime) {
        shootTimer += deltaTime;
        player.handleInput(deltaTime);

        if (Gdx.input.isKeyPressed(Input.Keys.F) && shootTimer >= FIRE_RATE) {
            player.shooting = true;
            level1Screen.bullets.add(spawnBullet());
            shootTimer = 0;
        } else if (!Gdx.input.isKeyPressed(Input.Keys.F))
            player.shooting = false;

        //Music Control
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            GameClass.isMusicPaused = !GameClass.isMusicPaused;
        }
    }

    private void checkBulletsPosition() {
        for (Bullet bullet : bullets) {
            if (bullet.getPosition() > gameCam.position.x + 2.2 || bullet.getPosition() < gameCam.position.x - 2.2) {
                bullet.setToDestroy();
            }
        }
    }


    public void bulletHitWall(Fixture fixture) {
        for (Bullet bullet : bullets) {
            if (bullet.equals(fixture.getUserData()))
                bullet.setToDestroy();
        }
    }

    public void bulletHitEnemy(Fixture enemyFixture, Fixture bulletFixture) {
        for (Bullet bullet : bullets) {
            if (bullet.equals(bulletFixture.getUserData()))
                bullet.setToDestroy();
        }
        for (Enemy enemy : enemies) {
            if (enemy.equals(enemyFixture.getUserData())) {
                if (enemy.setToDestroy())
                    player.increaseScore();
            }
        }
    }

    private void gameOver(boolean playerState) {
        gameClass.beginOutro(playerState);
    }
}
