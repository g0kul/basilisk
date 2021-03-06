package com.androidsrc.snake_game.snakegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.androidsrc.snake_game.actors.PositionedActor;
import com.androidsrc.snake_game.panels.AbstractGamePanel;


public class AppleActor extends PositionedActor {
	
	public static final int DRAW_SIZE = 25;

	public AppleActor(int x, int y, int colour) {
		super(x, y, DRAW_SIZE, DRAW_SIZE, colour);
	}

	@Override
	public void stylePaint(Paint p, int colour) {
		p.setColor(colour);
		p.setStyle(Style.FILL);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRoundRect(getRectF(), 10, 10, getPaint());
	}
	
	public void reposition(AbstractGamePanel panel) {
		setPos(randomCoordForPanel(panel.getWidth()), randomCoordForPanel(panel.getHeight()));
	}

	protected int randomCoordForPanel(int max) {
		int multiplier = max / DRAW_SIZE;
		int randomCoordinate = (int) (Math.random() * multiplier);
		return randomCoordinate * DRAW_SIZE;
	}

}
