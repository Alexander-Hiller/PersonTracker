package PersonTracker.Objects

import scalafx.scene.shape.Shape

class roomNode(val name: String, val inShape: Shape) extends thing(name, inShape) {
  var xPos: Double = 0
  var yPos: Double = 0
  var xDirection: Double = 0
  var yDirection: Double = 0
    //use the width variable to describe radius
  var width: Double = 5
  var height: Double = 0
  var shape:Shape = inShape
  override def toString: String ={
    this.name
  }
}
