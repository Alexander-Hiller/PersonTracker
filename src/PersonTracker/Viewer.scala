
import PersonTracker.Objects._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
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


object Viewer extends JFXApp {
  //graphics variables
  var sceneGraphics: Group = new Group {}
  val windowWidth: Double = 1200
  val windowHeight: Double = 800
  val userCircleRadius:Double =10
  val roomCircleRadius:Double =20
  //delta variable is the uncertainty of a measurement in pixels
  val delta: Double = 10

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
    drawRoomNode(225,150,"RoomNode_1", 15)

    drawWall(600,300,"WestWall_2",5,400)
    drawWall(800,500,"SouthWall_2",400,5)
    drawWall(1000,300,"EastWall_2",5,400)
    drawWall(800,100,"NorthWall_2",400,5)
    drawDoor(1000,300,"EastDoor_2",5,50)
    drawRoomNode(975,475,"RoomNode_2_1", 15)
    drawRoomNode(650,150,"RoomNode_2_2", 15)

    drawWall(600,600,"SouthWall_hallway",1200,5)
    drawRoomNode(100,620,"Hall_1", 15)
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
    tempDoor.xPos=centerX //- w / 2.0
    tempDoor.yPos=centerY //- l / 2.0
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
      radius = userCircleRadius
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
    val minNewBubble = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = w - delta
      fill = Color.Orange
      opacity = 0.1
    }
    val maxNewBubble = new Circle() {
      centerX = xVal
      centerY = yVal
      radius = w + delta
      fill = Color.Blue
      opacity = 0.1
    }
    val minTempBubble: thing = new bubble(name,minNewBubble)
    val maxTempBubble: thing = new bubble(name,maxNewBubble)
    minTempBubble.xPos=xVal
    maxTempBubble.xPos=xVal
    minTempBubble.yPos=yVal
    maxTempBubble.yPos=yVal
    //store radius in variable
    minTempBubble.width = w - delta
    maxTempBubble.width = w + delta
    allBubbles+= minTempBubble
    allBubbles+= maxTempBubble
    sceneGraphics.children.add(minNewBubble)
    sceneGraphics.children.add(maxNewBubble)
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
      //var html = Await.result(get("http://192.168.4.1/data"), 20.seconds)
      //println(html)
      screenUpdate()
    }
    AnimationTimer(update).start()
    // Start Animations. Calls update 60 times per second (takes update as an argument)
  }



  def get(uri: String) = {
    val request = HttpRequest(HttpMethods.GET, uri)
    for {
      response <- Http().singleRequest(request)
      content <- Unmarshal(response.entity).to[String]
    } yield content
  }
}