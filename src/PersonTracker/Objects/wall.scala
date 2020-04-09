package PersonTracker.Objects

import scalafx.scene.shape.Shape

class wall(val name: String, val inShape: Shape) extends thing(name, inShape) {
  var xPos: Double = 0
  var yPos: Double = 0
  var width: Double = 5
  var height: Double = 5
  var shape:Shape = inShape
  var xDirection: Double = 0
  var yDirection: Double = 0
  override def toString: String ={
    this.name
  }
}
