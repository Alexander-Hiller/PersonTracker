
import PersonTracker.Objects._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable
//testing new key
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

//graphics imports
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle, Shape}
import scalafx.scene.{Group, Scene}

//Todo: Uncertainty based on radius, measure between radius for extra vote, user specific, shift user circls apropriate distance (right and down), Why on one room



object Viewer extends JFXApp {
  //graphics variables
  var sceneGraphics: Group = new Group {}
  val windowWidth: Double = 1200
  val windowHeight: Double = 800
  val userCircleRadius:Double =10
  val roomCircleRadius:Double =20
  var signalScaler: Double = 1
  val pixPerFoot: Double = 23.265
  val doorUnlockRadius: Double = 100

  //http handlers
  import akka.http.scaladsl.unmarshalling.Unmarshal
  implicit val system = ActorSystem("http-client")
  implicit val materializer = ActorMaterializer()

  //list buffers of objects
  var allWalls = new ListBuffer[thing]()
  var allDoors = new ListBuffer[thing]()
  var allUsers = new ListBuffer[thing]()
  var allBubbles = new ListBuffer[thing]()
  var allRoomNodes = new ListBuffer[thing]()
  var allRooms = new ListBuffer[rooms]()
  var subRooms = new ListBuffer[rooms]()
  //map for timestamps
  var timeMap: scala.collection.mutable.Map[String, Double] = mutable.Map.empty[String, Double]
  def wallGen(): Unit = {
    //Delete Any Existing Barriers
    for (wall<- allWalls){
      sceneGraphics.children.remove(wall.shape)
      allWalls -= wall
    }
    drawWall(200,300,"WestWall_1",5,400)
    drawWall(400,500,"SouthWall_1",400,5)
    drawWall(600,300,"EastWall_1",5,400)
    drawWall(400,100,"NorthWall_1",400,5)
    drawDoor(400,500,"SouthDoor_1",50,5)
    //room nodes shall be named the same as the expected room node
    drawRoomNode(225,150,"Station4", 15)
    // add a room with name
    newRoom(200,100,600,500,"Left Room")

    drawWall(600,300,"WestWall_2",5,400)
    drawWall(800,500,"SouthWall_2",400,5)
    drawWall(1000,300,"EastWall_2",5,400)
    drawWall(800,100,"NorthWall_2",400,5)
    drawDoor(1000,300,"EastDoor_2",5,50)
    drawRoomNode(975,475,"Station2", 15)
    drawRoomNode(650,150,"Station1", 15)
    // add a room with name
    newRoom(600,100,1000,500,"Right Room")

    drawWall(600,600,"SouthWall_hallway",1200,5)
    drawRoomNode(100,620,"Station3", 15)
  }

  def newRoom(minX: Double, minY: Double, maxX: Double, maxY: Double, name:String): Unit = {
    val tempRoom: rooms = new rooms(name)
    tempRoom.xMin = minX
    tempRoom.xMax = maxX
    tempRoom.yMin = minY
    tempRoom.yMax = maxY
    allRooms += tempRoom
  }

  def newSubRoom(minX: Double, minY: Double, maxX: Double, maxY: Double, name:String): Unit = {
    val tempRoom: rooms = new rooms(name)
    tempRoom.xMin = minX
    tempRoom.xMax = maxX
    tempRoom.yMin = minY
    tempRoom.yMax = maxY
    subRooms += tempRoom
  }


  def drawWall(centerX: Double, centerY: Double, name:String, w: Double, h:Double): Unit = {
    val newWall = new Rectangle() {
      width=w
      height=h
      translateX = centerX - w / 2.0
      translateY = centerY - h / 2.0
      fill = Color.Black
    }
    val tempWall: thing = new wall(name,newWall)
    tempWall.xPos=centerX //- w / 2.0
    tempWall.yPos=centerY //- l / 2.0
    //store height and width in variables
    tempWall.width = w
    tempWall.height = h
    allWalls += tempWall
    sceneGraphics.children.add(newWall)
  }

  def drawDoor(centerX: Double, centerY: Double, name:String, w: Double, h:Double): Unit = {
    val newDoor = new Rectangle() {
      width=w
      height=h
      translateX = centerX - w / 2.0
      translateY = centerY - h / 2.0
      fill = Color.Red
    }
    val tempDoor: thing = new door(name,newDoor)
    tempDoor.xPos=centerX
    tempDoor.yPos=centerY
    //store height and width in variables
    tempDoor.width = w
    tempDoor.height = h
    allDoors += tempDoor
    sceneGraphics.children.add(newDoor)
  }

  def drawUser(xVal: Double, yVal: Double, name:String, w: Double): Unit = {
    val newUser = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = w
      fill = Color.Yellow
    }
    val tempUser: thing = new user(name,newUser)
    tempUser.xPos=xVal
    tempUser.yPos=yVal //- l / 2.0
    //store height and width in variables
    tempUser.width = userCircleRadius
    allUsers += tempUser
    sceneGraphics.children.add(newUser)
  }

  def drawRoomNode(xVal: Double, yVal: Double, name:String, w: Double): Unit = {
    val newRoomNode = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = roomCircleRadius
      fill = Color.Green
    }
    val tempRoomNode: thing = new roomNode(name,newRoomNode)
    tempRoomNode.xPos=xVal
    tempRoomNode.yPos=yVal //- l / 2.0
    //store radius in variable
    tempRoomNode.width = roomCircleRadius
    allRoomNodes += tempRoomNode
    sceneGraphics.children.add(newRoomNode)
  }

  def drawBubble(xVal: Double, yVal: Double, name:String, w: Double): Unit = {
    //delta variable is the uncertainty of a measurement in pixels
    val delta: Double = w*0.05 + 10

    val minNewBubble = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = w - delta
      fill = Color.Orange
      opacity = 0.1
    }
    val midNewBubble = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = w
      fill = Color.Red
      opacity = 0.01
    }
    val maxNewBubble = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = w + delta
      fill = Color.Blue
      opacity = 0.1
    }
    val minTempBubble: thing = new bubble(name,minNewBubble)
    val midTempBubble: thing = new bubble(name,midNewBubble)
    val maxTempBubble: thing = new bubble(name,maxNewBubble)
    minTempBubble.xPos=xVal
    midTempBubble.xPos=xVal
    maxTempBubble.xPos=xVal
    minTempBubble.yPos=yVal
    midTempBubble.yPos=yVal
    maxTempBubble.yPos=yVal
    //store radius in variable
    minTempBubble.width = w - delta
    midTempBubble.width = w
    maxTempBubble.width = w + delta
    allBubbles+= minTempBubble
    allBubbles+= midTempBubble
    allBubbles+= maxTempBubble
    sceneGraphics.children.add(minNewBubble)
    sceneGraphics.children.add(midNewBubble)
    sceneGraphics.children.add(maxNewBubble)
  }

  // how we determine where the user is
  def localize(): Unit = {
    var tempCenterX: Int = 0
    var tempCenterY: Int = 0
    var tempRadius: Double = 0
    var tempX: Int = 0
    var tempY: Int = 0
    var tempFit: Double = 0
    var roomFlag: Int = 0
    var deg: Int = 0;
    var maxVote: Double = 0;

    //cycle through all rooms
    for(room<-allRooms){
      room.vote = 0

      //if user was last seen in this room use it as a tie breaker
      for(user<-allUsers){
        if((user.xPos > room.xMin)&(user.xPos < room.xMax)&(user.yPos > room.yMin)&(user.yPos < room.yMax)){
          room.vote += 0.5
        }
      }

      //check all bubbles for edges in a room
      for(bubble <- allBubbles){
        tempCenterX = bubble.xPos.toInt
        tempCenterY = bubble.yPos.toInt
        tempRadius = bubble.width
        roomFlag = 0
        //val userName: Array[String] = bubble.toString.split("-")
        //println(userName(1))

        //another way, using trig
        for(deg <- 0 to 360){
          if (roomFlag<1) {
            val x: Double = bubble.xPos + bubble.width * math.sin(deg.toDouble * math.Pi / 180)
            val y: Double = (bubble.yPos - bubble.width * math.cos(deg.toDouble * math.Pi / 180))
            if ((x < room.xMax) & (x > room.xMin) & (y < room.yMax) & (y > room.yMin)) {
              room.vote += 1
              roomFlag = 1
            }
          }
        }

        /* //this way works... but not well
        for(tempX <- room.xMin.toInt to room.xMax.toInt){
          for(tempY <- room.yMin.toInt to room.yMax.toInt){
            if(roomFlag < 1) {
              tempFit = math.sqrt(math.pow((tempX - tempCenterX).toDouble, 2) + math.pow((tempY - tempCenterX).toDouble, 2))
              if ((tempFit < tempRadius + 2) & (tempFit > tempRadius - 2)) {
                room.vote += 1
                roomFlag = 1
              }
            }
          }
        }
        */
      }
      if(room.vote > maxVote)maxVote = room.vote
      println(room.toString + ": " + room.vote.toString)
    }

    // calculate a point based on all circles
    for(room<-allRooms){
      if(room.vote == maxVote){
        // split the room into 16 rectangles (4 x 4) "subRooms"
        val deltaX: Double = (room.xMax -room.xMin)/4
        val deltaY: Double = (room.xMax -room.xMin)/4
        for(i<- 0 to 3) {
          for(j<- 0 to 3) {
            newSubRoom(room.xMin + deltaX * j.toDouble, room.yMin + deltaY * i.toDouble, room.xMin + deltaX * (j + 1).toDouble, room.yMin + deltaY * (i + 1).toDouble, room.toString + "(" + j.toString + "," + i.toString + ")")
          }
        }
      }
    }


    //cycle through all sub rooms
    maxVote = 0
    for(room<-subRooms){
      room.vote = 0
      //if user was last seen in this room use it as a tie breaker
      for(user<-allUsers){
        if((user.xPos > room.xMin)&(user.xPos < room.xMax)&(user.yPos > room.yMin)&(user.yPos < room.yMax)){
          room.vote += 0.5
        }
      }
      //check all bubbles for edges in a room
      for(bubble <- allBubbles){
        tempCenterX = bubble.xPos.toInt
        tempCenterY = bubble.yPos.toInt
        tempRadius = bubble.width
        roomFlag = 0

        //another way, using trig
        for(deg <- 0 to 360){
          if (roomFlag<1) {
            val x: Double = bubble.xPos + bubble.width * math.sin(deg.toDouble * math.Pi / 180)
            val y: Double = (bubble.yPos - bubble.width * math.cos(deg.toDouble * math.Pi / 180))
            if ((x < room.xMax) & (x > room.xMin) & (y < room.yMax) & (y > room.yMin)) {
              room.vote += 1
              roomFlag = 1
            }
          }
        }

        /* //this way works... but not well
        for(tempX <- room.xMin.toInt to room.xMax.toInt){
          for(tempY <- room.yMin.toInt to room.yMax.toInt){
            if(roomFlag < 1) {
              tempFit = math.sqrt(math.pow((tempX - tempCenterX).toDouble, 2) + math.pow((tempY - tempCenterX).toDouble, 2))
              if ((tempFit < tempRadius + 2) & (tempFit > tempRadius - 2)) {
                room.vote += 1
                roomFlag = 1
              }
            }
          }
        }
        */
      }
      if(room.vote > maxVote)maxVote = room.vote
      //println(room.toString + ": " + room.vote.toString)
    }

    //delete all sub rooms when done
    //temp delete all users until I add code to track better
    for(user<-allUsers){
      allUsers -= user
      sceneGraphics.children.remove(user.shape)
    }
    for(room<-subRooms) {
      if(room.vote == maxVote){
        val deltaX: Double = (room.xMax -room.xMin)/4
        val deltaY: Double = (room.yMax -room.yMin)/4
        drawUser(room.xMin + 2* deltaX,room.yMin + 2* deltaY,room.toString, deltaY)
        // unlock door if user is near
        for(door<-allDoors){
          val xD: Double = door.xPos - (room.xMin+ 2* deltaX)
          val yD: Double = door.yPos - (room.yMin + 2* deltaY)
          val dist: Double = Math.sqrt((xD*xD)+(yD*yD))

          if(dist <= doorUnlockRadius){
            door.shape.fill = Color.ForestGreen
          }
          else if(dist <= 2 * doorUnlockRadius){
            door.shape.fill = Color.Yellow
          }
          else door.shape.fill = Color.Red
        }
      }
    subRooms-=room
    }


  }




  def screenUpdate(): Unit = {
    //first clear all bubbles
    for(bubble <- allBubbles){
      sceneGraphics.children.remove(bubble.shape)
      allBubbles -= bubble
    }

    //This is for testing. it moves the user
    for (user <- allUsers) {
      //x Movement
      if (user.xDirection == 1) {
        if (user.xPos < 1100) {
          user.shape.translateX.value += 5
          user.xPos += 5
        }
        else {
          user.xDirection = 0
        }
      }
      else {
        if (user.xPos > 100) {
          user.shape.translateX.value -= 5
          user.xPos -= 5
        }
        else {
          user.xDirection = 1
        }
      }
      //y Movement
      if (user.yDirection == 1) {
        if (user.yPos < 750) {
          user.shape.translateY.value += 5
          user.yPos += 5
        }
        else {
          user.yDirection = 0
        }
      }
      else {
        if (user.yPos > 50) {
          user.shape.translateY.value -= 5
          user.yPos -= 5
        }
        else {
          user.yDirection = 1
        }
      }
      //DrawBubbles based on user position
        for(room <- allRoomNodes)
          {
            //distance calc from node
            val nodeDistance: Double = math.sqrt(math.abs(room.xPos-user.xPos)*math.abs(room.xPos-user.xPos)+math.abs(room.yPos-user.yPos)*math.abs(room.yPos-user.yPos))
            val bubName: String = room.toString + user.toString
            drawBubble(room.xPos,room.yPos,bubName,nodeDistance)
          }


    }

  }
  this.stage = new PrimaryStage {
    //window setup
    this.title = "High Security Person Tracker"
    wallGen()
    scene = new Scene(windowWidth, windowHeight) {
      content = List(sceneGraphics)
    }
    //User uses keyboard
      addEventHandler(KeyEvent.KEY_PRESSED, (event: KeyEvent) => keyPressed(event.getCode))
      // event handler for user mouse click... Just using it now to spawn users
      addEventHandler(MouseEvent.MOUSE_CLICKED, (event: MouseEvent) => {
        val bullNum:Double=Math.random()*1000
        var xPos:Double=event.getX
        var yPos:Double =event.getY
        //the radius is is useless in this function but kept it for easier copypasta for bubble radius
        drawUser(xPos,yPos,"Clicked User", 5)
      })

    val update: Long => Unit = (time: Long) => {
      //update code here
      var json = Await.result(get("http://192.168.4.1/data"), 20.seconds)
      println(json)
      fromJSON(json)
      localize()
      //screenUpdate()
    }
    AnimationTimer(update).start()
    // Start Animations. Calls update 60 times per second (takes update as an argument)
  }

  def fromJSON(jsonState: String): Unit = {
    //variables
    var tempX: Double = 0;
    var tempY: Double = 0;


    // make lists of objects by type
    val parsed: JsValue = Json.parse(jsonState)


    val elements = (parsed \ "Data").as [Map[String,JsValue]]
    for(elem<-elements.keys) {
      val station = (elements(elem) \ "station").as[String]
      val user = (elements(elem) \ "user").as[String]
      var strength = (elements(elem) \ "strength").as[Double]
      val bubbleName = station + "-" + user
      val sTime = (elements(elem) \ "time").as[String]
      val time: Double = sTime.toDouble
      //println("station: " + station + "   Time: " + time.toString)
      //evaluate the timestamp
      if (timeMap.contains(bubbleName)) {
        var tempTime: Double = timeMap(bubbleName)
        if (tempTime < time) timeMap(bubbleName) = time
      }
      else timeMap += (bubbleName -> time)

      //if it's the newest reading
      if (timeMap(bubbleName) == time) {

        //strength transformation
        strength += 56
        strength *= (-1.0792)
        if (strength < 0) strength = 1
        strength *= signalScaler
        strength *= pixPerFoot
        //println(strength.toString)

        //search for the station in the existing nodes
        for (room <- allRoomNodes) {
          //When the room node is found then extract x,y info
          if (station == room.toString) {
            tempX = room.xPos
            tempY = room.yPos
          }
        }

        //search existing bubbles
        for (bubble <- allBubbles) {
          //if the bubble exists: destroy it
          if (bubble.toString == bubbleName) {
            sceneGraphics.children.remove(bubble.shape)
            allBubbles -= bubble
          }
        }

        //draw the new bubble
        drawBubble(tempX, tempY, bubbleName, strength)
      }
    }
  }


  def keyPressed(keyCode: KeyCode): Unit = {

        keyCode.getName match {
          case "Up" | "W" =>signalScaler+=0.1
          case "Down"| "S" => signalScaler-=0.1
          //case _ => println (keyCode.getName + " pressed with no action")
        }
  }


  def get(uri: String) = {
    val request = HttpRequest(HttpMethods.GET, uri)
    for {
      response <- Http().singleRequest(request)
      content <- Unmarshal(response.entity).to[String]
    } yield content
  }
}