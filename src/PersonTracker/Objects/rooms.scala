package PersonTracker.Objects



class rooms(name: String) {
  var xMin: Double = 0
  var yMin: Double = 0
  var xMax: Double = 0
  var yMax: Double = 0
  var vote: Double = 0
  override def toString: String ={
    this.name
  }
}