package com.ruoyi.web.service.video.util;

import java.awt.image.BufferedImage;

/**
 * 视频检测工具类
 * 提供视频模型检测器共用的工具方法
 * 
 * @author ruoyi
 */
public class VideoDetectionUtils {

    private VideoDetectionUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 计算两帧之间的差异度
     * 
     * @param frame1 第一帧
     * @param frame2 第二帧
     * @return 差异度（0-1之间）
     */
    public static double calculateFrameDifference(BufferedImage frame1, BufferedImage frame2) {
        if (frame1 == null || frame2 == null) {
            return 0.0;
        }

        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());

        long totalDiff = 0;
        int pixelCount = 0;
        int step = Math.max(1, width / 50); // 采样以提高性能

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb1 = frame1.getRGB(x, y);
                int rgb2 = frame2.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;

                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;

                totalDiff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                pixelCount++;
            }
        }

        return pixelCount > 0 ? (double) totalDiff / (pixelCount * 3 * 255) : 0.0;
    }

    /**
     * 计算像素亮度
     * 
     * @param rgb RGB值
     * @return 亮度值（0-255）
     */
    public static int getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    /**
     * 判断是否为边缘点
     * 
     * @param image 图像
     * @param x x坐标
     * @param y y坐标
     * @return 是否为边缘点
     */
    public static boolean isEdgePoint(BufferedImage image, int x, int y) {
        if (x <= 0 || y <= 0 || x >= image.getWidth() - 1 || y >= image.getHeight() - 1) {
            return false;
        }

        int center = getBrightness(image.getRGB(x, y));
        int threshold = 30;

        // 检查周围8个点
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            int neighbor = getBrightness(image.getRGB(nx, ny));

            if (Math.abs(center - neighbor) > threshold) {
                return true;
            }
        }

        return false;
    }

    /**
     * 计算帧的平均亮度
     * 
     * @param frame 帧图像
     * @return 平均亮度（0-255）
     */
    public static double calculateAverageBrightness(BufferedImage frame) {
        if (frame == null) {
            return 0.0;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        long totalBrightness = 0;
        int pixelCount = 0;
        int step = Math.max(1, width / 50);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                totalBrightness += getBrightness(frame.getRGB(x, y));
                pixelCount++;
            }
        }

        return pixelCount > 0 ? (double) totalBrightness / pixelCount : 0.0;
    }

    /**
     * 计算帧的对比度
     * 
     * @param frame 帧图像
     * @return 对比度值
     */
    public static double calculateContrast(BufferedImage frame) {
        if (frame == null) {
            return 0.0;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        double avgBrightness = calculateAverageBrightness(frame);
        
        long varianceSum = 0;
        int pixelCount = 0;
        int step = Math.max(1, width / 50);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int brightness = getBrightness(frame.getRGB(x, y));
                double diff = brightness - avgBrightness;
                varianceSum += diff * diff;
                pixelCount++;
            }
        }

        return pixelCount > 0 ? Math.sqrt(varianceSum / pixelCount) : 0.0;
    }

    /**
     * 计算颜色饱和度
     * 
     * @param frame 帧图像
     * @return 饱和度值（0-1）
     */
    public static double calculateSaturation(BufferedImage frame) {
        if (frame == null) {
            return 0.0;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        double totalSaturation = 0.0;
        int pixelCount = 0;
        int step = Math.max(1, width / 50);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = frame.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));

                if (max > 0) {
                    totalSaturation += (double) (max - min) / max;
                }
                pixelCount++;
            }
        }

        return pixelCount > 0 ? totalSaturation / pixelCount : 0.0;
    }

    /**
     * 计算边缘密度
     * 
     * @param frame 帧图像
     * @return 边缘密度（0-1）
     */
    public static double calculateEdgeDensity(BufferedImage frame) {
        if (frame == null) {
            return 0.0;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        int edgeCount = 0;
        int totalCount = 0;
        int step = Math.max(1, width / 30);

        for (int y = step; y < height - step; y += step) {
            for (int x = step; x < width - step; x += step) {
                if (isEdgePoint(frame, x, y)) {
                    edgeCount++;
                }
                totalCount++;
            }
        }

        return totalCount > 0 ? (double) edgeCount / totalCount : 0.0;
    }

    /**
     * 计算颜色丰富度
     * 
     * @param frame 帧图像
     * @return 颜色丰富度（0-1）
     */
    public static double calculateColorRichness(BufferedImage frame) {
        if (frame == null) {
            return 0.0;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        java.util.Set<Integer> colors = new java.util.HashSet<>();
        int step = Math.max(1, width / 50);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = frame.getRGB(x, y);
                // 将RGB简化到16级以减少内存占用
                int r = ((rgb >> 16) & 0xFF) / 16;
                int g = ((rgb >> 8) & 0xFF) / 16;
                int b = (rgb & 0xFF) / 16;
                colors.add((r << 8) | (g << 4) | b);
            }
        }

        // 最多4096种简化颜色
        return Math.min(1.0, colors.size() / 2048.0);
    }

    /**
     * 计算纹理复杂度
     * 
     * @param frame 帧图像
     * @return 纹理复杂度（0-1）
     */
    public static double calculateTextureComplexity(BufferedImage frame) {
        if (frame == null) {
            return 0.0;
        }

        // 结合边缘密度和对比度来评估纹理复杂度
        double edgeDensity = calculateEdgeDensity(frame);
        double contrast = calculateContrast(frame);
        
        // 归一化对比度到0-1范围
        double normalizedContrast = Math.min(1.0, contrast / 100.0);
        
        return (edgeDensity + normalizedContrast) / 2.0;
    }
}
