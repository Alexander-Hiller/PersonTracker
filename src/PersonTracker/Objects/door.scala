package PersonTracker.Objects

import scalafx.scene.shape.Shape

class door(val name: String, val inShape: Shape) extends thing(name, inShape) {
  var xPos: Double = 0
  var yPos: Double = 0
  var width: Double = 5
  var height: Double = 5
  var shape:Shape = inShape
  override def toString: String ={
    this.name
  }
}
