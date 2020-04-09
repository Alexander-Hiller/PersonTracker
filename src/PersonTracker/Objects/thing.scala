package PersonTracker.Objects

import scalafx.scene.shape.Shape

abstract class thing(name: String, inShape: Shape) {
  var xPos: Double
  var yPos: Double
  var width: Double
  var height: Double
  var xDirection: Double
  var yDirection: Double
  var shape:Shape
}