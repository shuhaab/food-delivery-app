package com.g25.customcomponent;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class CellContainer extends StackPane {
  public CellContainer(Node node) {
    super(node);
    setPadding(new Insets(10));
    getStyleClass().add("cell-container");
  }
}
