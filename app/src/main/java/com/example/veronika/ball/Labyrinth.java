package com.example.veronika.ball;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by veronika on 26/03/2017.
 */

public class Labyrinth {
    class Point {
        int x, y;
        Point(int nx, int ny) {
            x = nx;
            y = ny;
        }
    }
    class Wall {
        Point begin, end;
        Wall(int nx0, int ny0, int nx1, int ny1) {
            begin = new Point(nx0, ny0);
            end = new Point(nx1, ny1);
        }
    };
    ArrayList <Wall> walls;
    Point start, finish;
    Point size;

    void readWalls(Context context) {
        walls = new ArrayList<>();
        try {
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream = context.getResources().openRawResource(R.raw.map);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    StringReader lr = new StringReader(line);
                    Scanner lrs = new Scanner(lr);
                    String kind = lrs.next();
                    if (kind.equals("//")) {
                        continue;
                    }
                    if (kind.equals("w")) {
                        int x0 = lrs.nextInt();
                        int x1 = lrs.nextInt();
                        int y0 = lrs.nextInt();
                        int y1 = lrs.nextInt();
                        walls.add(new Wall(x0, x1, y0, y1));
                    }

                    if (kind.equals("start")) {
                        int x0 = lrs.nextInt();
                        int y0 = lrs.nextInt();
                        start = new Point(x0, y0);
                    }

                    if (kind.equals("finish")) {
                        int x0 = lrs.nextInt();
                        int y0 = lrs.nextInt();
                        finish = new Point(x0, y0);
                    }

                    if (kind.equals("size")) {
                        int x0 = lrs.nextInt();
                        int y0 = lrs.nextInt();
                        size = new Point(x0, y0);
                    }

                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Resources.NotFoundException nfex) {
            nfex.printStackTrace();
        }
    }

    boolean cross (int fb, int fe, int sb, int se) {
        return ( ( Math.min(fb, fe) < Math.max(sb, se) ) != ( Math.max(fb, fe) < Math.min(sb, se) ) );
    }

    public void draw(Canvas canvas, Ball ball, int width, int height) {

        //PREPARE TO DRAW
        final float min_dim = Math.min(width, height);
        final float x_viewPoint = (float) width * 100.0f / min_dim;
        final float y_viewPoint = (float) height * 100.0f / min_dim;
        final int x_view_min = (int) Math.floor(ball.getX() - x_viewPoint * 0.5f);
        final int x_view_max = (int) Math.ceil(ball.getX() + x_viewPoint * 0.5f);
        final int y_view_min = (int) Math.floor(ball.getY() - y_viewPoint * 0.5f);
        final int y_view_max = (int) Math.ceil(ball.getY() + y_viewPoint * 0.5f);
        ArrayList <Wall> vis_walls = getVisibleWalls(x_view_min, y_view_min, x_view_max, y_view_max);

        //WALLS
        Paint wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaint.setStrokeWidth(min_dim * 0.02f);
        wallPaint.setColor(0xffbf1faf);
        for (int i = 0; i < vis_walls.size(); ++i) {
            Wall wall = vis_walls.get(i);
            float x0 = (wall.begin.x - x_view_min) * (float) width  / x_viewPoint;
            float y0 = (wall.begin.y - y_view_min) * (float) height / y_viewPoint;
            float x1 = (wall.end.x   - x_view_min) * (float) width  / x_viewPoint;
            float y1 = (wall.end.y   - y_view_min) * (float) height / y_viewPoint;
            canvas.drawLine(x0, y0, x1, y1, wallPaint);
        }

        //START AND FINISH POINTS
        Paint positionsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        positionsPaint.setColor(0xffaa0011);
        final float ball_radius = min_dim / 10.0f;
        float x_start  = (start.x - x_view_min)  * (float) width  / x_viewPoint;
        float y_start  = (start.y - y_view_min)  * (float) height / y_viewPoint;
        float x_finish = (finish.x - x_view_min) * (float) width  / x_viewPoint;
        float y_finish = (finish.y - y_view_min) * (float) height / y_viewPoint;
        Log.i("Labyrinth.draw", String.format("start=%.2f:%.2f finish=%.2f:%.2f", x_start, y_start, x_finish, y_finish));
        canvas.drawCircle(x_start, y_start, ball_radius, positionsPaint);
        canvas.drawCircle(x_finish, y_finish, ball_radius, positionsPaint);
    }

    Wall wallTouched (ArrayList<Wall> vis_walls, float ball_x, float ball_y) {
        for (int i = 0; i < vis_walls.size(); i++) {
            if (wallIsTouched(vis_walls.get(i), ball_x, ball_y)) {
                return vis_walls.get(i);
            }
        }
        return null;
    }

    boolean wallIsTouched(Wall wall, float ball_x, float ball_y) {
            float a = wall.end.y - wall.begin.y;
            float b = wall.begin.x - wall.end.x;
            float c = wall.end.y * (wall.end.x - wall.begin.x) -
                      wall.begin.x * (wall.end.y - wall.begin.y);
            float l = (float) (Math.abs(a*ball_x + b*ball_y + c) / Math.hypot(a, b));
        return (l < 10.0f);
    }

    void checkWallTouchAndReact(Drawer drawer) {
        Ball ball = drawer.ball;
        Labyrinth.Wall touched_wall = wallTouched(
                getWallsVisibleAtScreen(
                        ball.getX(), ball.getY(),
                        drawer.getWidth(), drawer.getHeight()),
                ball.getX(), ball.getY());
        if (touched_wall != null) {
            ball.collision(touched_wall);
        }
    }

    ArrayList<Wall> getWallsVisibleAtScreen(float x, float y, int width, int height) {
        final float min_dim = Math.min(width, height);
        final float x_viewPoint = (float) width * 100.0f / min_dim;
        final float y_viewPoint = (float) height * 100.0f / min_dim;
        final int x_view_min = (int) Math.floor(x - x_viewPoint * 0.5f);
        final int x_view_max = (int) Math.ceil(x + x_viewPoint * 0.5f);
        final int y_view_min = (int) Math.floor(y - y_viewPoint * 0.5f);
        final int y_view_max = (int) Math.ceil(y + y_viewPoint * 0.5f);
        ArrayList <Wall> vis_walls = getVisibleWalls(x_view_min, y_view_min, x_view_max, y_view_max);
        return vis_walls;
    }

    ArrayList <Wall> getVisibleWalls(int x0, int y0, int x1, int y1) {
        ArrayList<Wall> visible = new ArrayList<>();
        for (int i = 0; i < walls.size(); i++) {
            Wall current_wall = walls.get(i);
            if (cross(current_wall.begin.x, current_wall.end.x, x0, x1) &&
                    cross(current_wall.begin.y, current_wall.end.y, y0, y1)) {
                visible.add(current_wall);
            }
        }
        return visible;
    }
}
