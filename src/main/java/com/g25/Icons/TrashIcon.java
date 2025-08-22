package com.g25.Icons;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class TrashIcon {
    public Group svg;

    public TrashIcon() {
        Group root = new Group();

        Line topLine = new Line(3, 6, 21, 6);
        topLine.setStroke(Color.BLACK);
        topLine.setStrokeWidth(2);

        SVGPath trashBody = new SVGPath();
        trashBody.setContent("M19 6 v14 c0 1 -1 2 -2 2 H7 c-1 0 -2 -1 -2 -2 V6");
        trashBody.setStroke(Color.BLACK);
        trashBody.setFill(null); // no fill
        trashBody.setStrokeWidth(2);
        trashBody.setStrokeLineCap(StrokeLineCap.ROUND);
        trashBody.setStrokeLineJoin(StrokeLineJoin.ROUND);

        SVGPath trashLid = new SVGPath();
        trashLid.setContent("M8 6 V4 c0 -1 1 -2 2 -2 h4 c1 0 2 1 2 2 v2");
        trashLid.setStroke(Color.BLACK);
        trashLid.setFill(null);
        trashLid.setStrokeWidth(2);
        trashLid.setStrokeLineCap(StrokeLineCap.ROUND);
        trashLid.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Line innerLineLeft = new Line(10, 11, 10, 17);
        innerLineLeft.setStroke(Color.BLACK);
        innerLineLeft.setStrokeWidth(2);

        Line innerLineRight = new Line(14, 11, 14, 17);
        innerLineRight.setStroke(Color.BLACK);
        innerLineRight.setStrokeWidth(2);

        root.getChildren().addAll(topLine, trashBody, trashLid, innerLineLeft, innerLineRight);

        this.svg = root;
    }
}