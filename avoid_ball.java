import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.*;


public class avoid_ball{
    static JFrame f = new JFrame("我是弹球游戏的窗口名称");;
    static Random rand = new Random();
    static ArrayList<Ball> balls = new ArrayList<>();
    static int ball_num = 20;
    static int mouseX = 0;
    static int mouseY = 0;
    static Cube cube;
    static boolean is_pause = true;
    static Instant begin_time;

    public static int get_rand(int min, int max){
        int i = min + rand.nextInt(max - min + 1);
        return i;
    }

    public static void main(String[] args){
        f = new JFrame("我是弹球游戏的窗口名称");
		f.setSize(800, 600);
		f.setLayout(new BorderLayout());
        f.setLocationRelativeTo(null);
		f.getContentPane().setBackground(new Color(238, 244, 249));
        f.setVisible(true);

        //创建游戏面板(小球区)
        GameArea game_area = new GameArea();
        game_area.addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e) {
                if(is_pause == false){
                    mouseX = e.getX();
                    mouseY = e.getY();
                }
            }
        });
        f.add(game_area, BorderLayout.CENTER);

        //隐藏光标
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(0, 0);
        BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
        Cursor cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "InvisibleCursor");

        //输入框
        JTextField input = new JTextField("20");
        input.setFont(new Font(null, 0, 20));
        input.setPreferredSize(new Dimension(75, 50));
        input.addActionListener(l -> {
            String num = input.getText();
            try{
                int n = Integer.parseInt(num);
                if(n <= 0){
                    throw new Exception();
                }
                ball_num = n;
                JOptionPane.showMessageDialog(f, "修改成功");
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(f, "请输入正整数!!!");
                input.setText("20");
            }
        });

        
        //添加"开始"按钮
        JPanel panel = new JPanel();
        JButton begin_but = new JButton("开始");
        begin_but.setFont(new Font(null, 0, 20));
        begin_but.setPreferredSize(new Dimension(75, 50));
        begin_but.addActionListener(l ->{
            String num = input.getText();
            try{
                int n = Integer.parseInt(num);
                if(n <= 0){
                    n = 20;
                    input.setText("20");
                }
                ball_num = n;
            }
            catch(Exception e){
                input.setText("20");
            }
            play(game_area, cursor);
            game_area.repaint();
        });

        panel.setLayout(new FlowLayout());
        panel.setBackground(new Color(208, 204, 209));
        panel.add(input);
        panel.add(begin_but);
        f.add(panel, BorderLayout.NORTH);
        f.validate();

        f.add(game_area, BorderLayout.CENTER);
    }

    public static void play(GameArea game_area, Cursor cursor){
        //将光标移动到游戏区
        try{
            Point pos = game_area.getLocationOnScreen();
            Robot robot = new Robot();
            robot.mouseMove(pos.x + game_area.getWidth() / 2, pos.y + game_area.getHeight() / 2);
            mouseX = game_area.getWidth() / 2;
            mouseY = game_area.getHeight() / 2;
        }
        catch(AWTException e){}

        is_pause = false;
        begin_time = Instant.now();
        //隐藏光标
        game_area.setCursor(cursor);

        balls.clear();

        cube = new Cube(game_area);
        new Thread(cube).start();

        for (int i = 0; i < ball_num; i++){
            balls.add(new Ball(game_area));
        }
        for (Ball ball: balls){
            new Thread(ball).start();
        }

        new Thread(game_area).start();
    }

    public static class GameArea extends JPanel implements Runnable{
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D gg = (Graphics2D)g;
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Ball ball: balls){
                ball.draw(gg);
            }
            if (cube !=null){
                cube.draw(gg);
            }
            
        }

        public void run(){
            while (is_pause == false) {
                repaint();
                try {
                     Thread.sleep(8);
                } 
                catch (InterruptedException e) {}
            }
            Instant end_time = Instant.now();
            long seconds = end_time.getEpochSecond() - begin_time.getEpochSecond();
            JOptionPane.showMessageDialog(f, "游戏结束！存活时间: " + seconds + "秒");
            //System.exit(0);
        }
    }

    public static class Ball implements Runnable{
        float speed = 0;
        float posX = 0;
        float posY = 0;
        int radius = 15;
        double dirX = 0;
        double dirY = 0;
        Color color;
        float width = 0;
        float height = 0;

        Ball(GameArea game_area){
            speed = get_rand(2, 3);
            radius = get_rand(14, 20);
            width = game_area.getWidth();
            height = game_area.getHeight();
            posX = get_rand(radius, (int)width - 2 * radius);
            posY = get_rand(radius, (int)height - 2 * radius);
            float i = (float)get_rand(0, 628) / 100;
            dirX = Math.cos(i);
            dirY = Math.sin(i);
            color = new Color(get_rand(0, 205), get_rand(0, 205), get_rand(0, 205));
        }
        
        void draw(Graphics2D g){
            g.setColor(color);
            g.fill(new Ellipse2D.Float(posX, posY, radius * 2, radius * 2));
        }

        public void run(){
            while (is_pause == false) {
                posX += speed * dirX;
                posY += speed * dirY;
                if (posX <= 0 || posX >= width - 2 * radius){
                    dirX *= -1;
                }
                if (posY <= 0 || posY >= height - 2 * radius){
                    dirY *= -1;
                }
                check();

                try {
                     Thread.sleep(8);
                } 
                catch (InterruptedException e) {}
            }
        }

        public void check(){
            float dx = posX + radius - mouseX;
            float dy = posY + radius - mouseY;
            if (dx*dx + dy*dy <= (radius + 6) * (radius + 6)){
                    Instant end_time = Instant.now();
                    long seconds = end_time.getEpochSecond() - begin_time.getEpochSecond();
                    //若开始游戏一秒内碰到球，不中止游戏，防止游戏开始时小球与鼠标重叠直接结束
                    if(seconds > 1){
                        is_pause = true;
                    }
            }
        }
    }

    public static class Cube implements Runnable{
        float size = 15.0f;
        float width = 0;
        float height = 0;

        Cube(GameArea game_area){
            width = game_area.getWidth();
            height = game_area.getHeight();
        }

        void draw(Graphics2D g){
            g.setColor(new Color(255, 0, 0));
            g.fill(new Rectangle.Float(mouseX - size / 2, mouseY- size / 2, size, size));
        }

        public void run(){
            while (is_pause == false) {
                check();
                try {
                     Thread.sleep(4);
                } 
                catch (InterruptedException e) {}
            }
        }

        public void check(){
            if ((mouseX - 7.5< 0 || mouseX + 7.5 > width || mouseY - 7.5 < 0 || mouseY + 7.5 > height)){
                is_pause = true;
            }
        }
    }
}
