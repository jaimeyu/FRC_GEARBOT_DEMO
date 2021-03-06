package org.usfirst.frc.team2706.robot.subsystems;

import org.usfirst.frc.team2706.robot.Robot;
import org.usfirst.frc.team2706.robot.commands.TankDriveWithJoystick;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The DriveTrain subsystem incorporates the sensors and actuators attached to
 * the robots chassis. These include four drive motors, a left and right encoder
 * and a gyro.
 */
public class DriveTrain extends Subsystem {
	private SpeedController front_left_motor, back_left_motor,
							front_right_motor, back_right_motor;
	private RobotDrive drive;
	private Encoder left_encoder, right_encoder;
	private AnalogInput rangefinder;
	private Gyro gyro;

	public DriveTrain() {
		super();
		front_left_motor = new Talon(1);
		back_left_motor = new Talon(2);
		front_right_motor = new Talon(3);
		back_right_motor = new Talon(4);
		drive = new RobotDrive(front_left_motor, back_left_motor,
							   front_right_motor, back_right_motor);
		left_encoder = new Encoder(1, 2);
		right_encoder = new Encoder(3, 4);
		
		// Encoders may measure differently in the real world and in
		// simulation. In this example the robot moves 0.042 barleycorns
		// per tick in the real world, but the simulated encoders
		// simulate 360 tick encoders. This if statement allows for the
		// real robot to handle this difference in devices.
		if (Robot.isReal()) {
			/* !!! @TODO: PLEASE TUNE THIS TO MATCH ENCODER TYPE !!! */
			left_encoder.setDistancePerPulse(0.042);
			right_encoder.setDistancePerPulse(0.042);
		} else {
			// Circumference in ft = 4in/12(in/ft)*PI
			left_encoder.setDistancePerPulse((4.0/12.0*Math.PI) / 360.0);
			right_encoder.setDistancePerPulse((4.0/12.0*Math.PI) / 360.0);
		}

		rangefinder = new AnalogInput(6);
		gyro = new Gyro(1);

		// Let's show everything on the LiveWindow
		LiveWindow.addActuator("Drive Train", "Front_Left Motor", (Talon) front_left_motor);
		LiveWindow.addActuator("Drive Train", "Back Left Motor", (Talon) back_left_motor);
		LiveWindow.addActuator("Drive Train", "Front Right Motor", (Talon) front_right_motor);
		LiveWindow.addActuator("Drive Train", "Back Right Motor", (Talon) back_right_motor);
		LiveWindow.addSensor("Drive Train", "Left Encoder", left_encoder);
		LiveWindow.addSensor("Drive Train", "Right Encoder", right_encoder);
		LiveWindow.addSensor("Drive Train", "Rangefinder", rangefinder);
		LiveWindow.addSensor("Drive Train", "Gyro", gyro);
	}

	/**
	 * When no other command is running let the operator drive around
	 * using the PS3 joystick.
	 */
	public void initDefaultCommand() {
		setDefaultCommand(new TankDriveWithJoystick());
	}

	/**
	 * The log method puts interesting information to the SmartDashboard.
	 */
	public void log() {
		SmartDashboard.putNumber("Left Distance", left_encoder.getDistance());
		SmartDashboard.putNumber("Right Distance", right_encoder.getDistance());
		SmartDashboard.putNumber("Combined distance", (left_encoder.getDistance()+ right_encoder.getDistance())/2);
		SmartDashboard.putNumber("Left Speed", left_encoder.getRate());
		SmartDashboard.putNumber("Right Speed", right_encoder.getRate());
		SmartDashboard.putNumber("Gyro Angle", gyro.getAngle());
		SmartDashboard.putNumber("Gyro Rate", gyro.getRate());
		SmartDashboard.putNumber("FL motor speed", front_left_motor.get());
		SmartDashboard.putNumber("BL motor speed", back_left_motor.get());
		SmartDashboard.putNumber("FR motor speed", front_right_motor.get());
		SmartDashboard.putNumber("BR motor speed", back_right_motor.get());
		SmartDashboard.putString("Left Encoder direction", left_encoder.getDirection() ? "FWRD": "BKWD");
		SmartDashboard.putString("Right Encoder direction", right_encoder.getDirection() ? "FWRD" : "BKWD");
		SmartDashboard.putNumber("BR motor speed", back_right_motor.get());
		SmartDashboard.putNumber("RangeFinder", getDistanceToObstacle());
		
	}

	/**
	 * Tank style driving for the DriveTrain. 
	 * @param left Speed in range [-1,1]
	 * @param right Speed in range [-1,1]
	 * 
	 */
	public void drive(double left, double right) {
		drive.drive(left, right);
	}

	/**
	 * @param joy Get the joystick and use arcade drive
	 * so the controls are intuitive.
	 * Tank drive was difficult to work with on the joystick
	 * during testing. 
	 * 
	 * @TODO Add throttle controls. Maybe have the joystick sensitivity
	 * reduce when a trigger is pulled?
	 * The Sidewinder has this ability using the physical throttle,
	 * but the xbox controls do not. 
	 */
	public void drive(Joystick joy) {
		drive.arcadeDrive(joy);
	}

	/**
	 * @return The robots heading in degrees.
	 */
	public double getHeading() {
		return gyro.getAngle();
	}

	/**
	 * Reset the robots sensors to the zero states.
	 */
	public void reset() {
		gyro.reset();
		left_encoder.reset();
		right_encoder.reset();
	}

	/**
	 * @return The distance driven (average of left and right encoders).
	 */
	public double getDistance() {
		return (left_encoder.getDistance() + right_encoder.getDistance())/2;
	}
	
	/**
	 * @return The distance to the obstacle detected by the rangefinder. 
	 * 
	 * @TODO: This needs to be tuned for reality and the type of range finder we use.
	 * eg: 1v = 0.1 meter. 
	 * 
	 * @TODO: Do we need to smooth this out? 
	 * @TODO: Should this be a smartdashboard tunable?
	 */
	public double getDistanceToObstacle() {
		// Really meters in simulation since it's a rangefinder...
		double distance = 0;
			distance = rangefinder.getAverageVoltage();
		if (!Robot.isSimulation()) {
			distance *= 0.1;/* !!! @TODO PLEASE TUNE THIS !! */
		}
		return distance;
	}
}
