package com.ld;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Video2char {

    public static void main(String[] args) throws IOException, InterruptedException {
        String videoPath = "E:\\桌面文件\\video2char\\video.mp4";
        String imageDir = "E:\\桌面文件\\video2char\\image\\";
        String charDir = "E:\\桌面文件\\video2char\\char\\";
        System.out.println("视频抓帧开始...");
        // 抓取视频帧图片 并保存到imageDir
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath);
        frameGrabber.start();
        int lengthInFrames = frameGrabber.getLengthInFrames();
        for (int i = 0; i < lengthInFrames; i++) {
            Frame imageFrame = frameGrabber.grabImage();
            if(imageFrame != null){
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bufferedImage = converter.getBufferedImage(imageFrame);
                ImageIO.write(bufferedImage, "jpg", new File(imageDir + i + ".jpg" ));
            }
        }
        frameGrabber.stop();
        frameGrabber.close();
        System.out.println("视频抓帧完毕，共" + lengthInFrames + "帧，" + "开始读取图片...");

        String charPart = "@MWXQRE%#&*=+~!^-.";
        // 读取图片文件目录全部文件，遍历转换成字符，并且保存字符文件到char目录
        File[] imageFiles = new File(imageDir).listFiles();
        if(imageFiles != null) {
            for (File imageFile : imageFiles) {
                // 创建字符文件
                FileWriter fileWriter = new FileWriter(charDir + imageFile.getName().replace(".jpg", "") + ".txt");
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                BufferedImage image = ImageIO.read(imageFile);
                for (int y = 0; y < image.getHeight(); y+=10) {
                    StringBuilder result = new StringBuilder();
                    for (int x = 0; x < image.getWidth(); x+=5) {
                        int pixel = image.getRGB(x, y);
                        int r = (pixel & 0xff0000) >> 16;
                        int g = (pixel & 0xff00) >> 8;
                        int b = (pixel & 0xff);
                        float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                        int index = Math.round(gray * (charPart.length() + 1) / 255);
                        result.append(index >= charPart.length()? " ":String.valueOf(charPart.charAt(index)));
                    }
                    bufferedWriter.write(result.append("\r\n").toString());
                }
                fileWriter.close();
            }
            System.out.println("图片转字符完毕，开始创建JFrame窗口播放字符动画...");
        }

        // 字符文件生成完毕，创建JFrame窗口，读取字符文件并且播放
        JFrame jFrame = new JFrame();
        jFrame.setTitle("字符播放");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setBounds(0, 0, 1920, 1210);
        jFrame.setResizable(true);
        jFrame.setUndecorated(false);
        JTextArea area = new JTextArea();
        area.setBackground(Color.white);
        area.setBounds(0, 0, 1920, 1210);
        area.setFont(new Font("宋体", Font.PLAIN, 9));
        jFrame.add(area);
        jFrame.setVisible(true);

        // 窗口创建完毕，读取字符文件
        File[] files = new File(charDir).listFiles();
        if (files != null) {
            List<File> fileList = Arrays.stream(files).sorted((o1, o2) -> {
                int index1 = Integer.parseInt(o1.getName().replace(".txt", ""));
                int index2 = Integer.parseInt(o2.getName().replace(".txt", ""));
                return index1 - index2;
            }).collect(Collectors.toList());
            for (File charFile : fileList) {
                StringBuilder result = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(charFile));
                String s;
                while ((s = br.readLine()) != null) {
                    result.append(System.lineSeparator()).append(s);
                }
                br.close();
                area.setText(result.toString());
                // 设置时间间隔
                Thread.sleep(20000/ files.length);
            }
        }
    }
}
