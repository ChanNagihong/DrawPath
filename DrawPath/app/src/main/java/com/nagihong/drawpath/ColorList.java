package com.nagihong.drawpath;

import android.graphics.Color;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by channagihong on 16/7/28.
 */
public class ColorList {

    public static List<Integer> mColors;
    public static List<Integer> mSelectedColors;
    private static String[] materialColorArray =
            new String[] {
                    "#C2185B",
                    "#7B1FA2",
                    "#303F9F",
                    "#388E3C",
                    "#FBC02D",
                    "#F8BBD0",
                    "#9C27B0",
                    "#3F51B5",
                    "#4CAF50",
                    "#FFEB3B",
                    "#FF5252",
                    "#E1BEE7",
                    "#C5CAE9",
                    "#C8E6C9",
                    "#FFF9C4",
                    "#E91E63",
                    "#7C4DFF",
                    "#448AFF",
                    "#8BC34A",
                    "#009688"
            };

    public static List<Integer> getColors() {
        if (null == mColors) {
            mColors = new LinkedList<>();
            for(String color : materialColorArray) {
                mColors.add(Color.parseColor(color));
            }
            mColors.add(Color.rgb(207, 248, 246)); //1
            mColors.add(Color.rgb(148, 212, 212)); //2
            mColors.add(Color.rgb(136, 180, 187)); //3
            mColors.add(Color.rgb(118, 174, 175)); //4
            mColors.add(Color.rgb(42, 109, 130)); //5
            mColors.add(Color.rgb(217, 80, 138)); //6
            mColors.add(Color.rgb(254, 149, 7)); //7
            mColors.add(Color.rgb(254, 247, 120)); //8
            mColors.add(Color.rgb(106, 167, 134)); //9
            mColors.add(Color.rgb(53, 194, 209)); //10
            mColors.add(Color.rgb(64, 89, 128)); //11
            mColors.add(Color.rgb(149, 165, 124)); //12
            mColors.add(Color.rgb(217, 184, 162)); //13
            mColors.add(Color.rgb(191, 134, 134)); //14
            mColors.add(Color.rgb(179, 48, 80)); //15
            mColors.add(Color.rgb(193, 37, 82)); //16
            mColors.add(Color.rgb(255, 102, 0)); //17
            mColors.add(Color.rgb(245, 199, 0)); //18
            mColors.add(Color.rgb(106, 150, 31)); //19
            mColors.add(Color.rgb(179, 100, 53)); //20
            mColors.add(Color.rgb(192, 255, 140)); //21
            mColors.add(Color.rgb(255, 247, 140)); //22
            mColors.add(Color.rgb(255, 208, 140)); //23
            mColors.add(Color.rgb(140, 234, 255)); //24
            mColors.add(Color.rgb(255, 140, 157)); //25
        }
        return mColors;
    }

    public static List<Integer> getSelectedColors() {
        if (null == mSelectedColors) {
            mSelectedColors = new LinkedList<>();
            mSelectedColors.add(Color.rgb(187, 228, 226)); //1
            mSelectedColors.add(Color.rgb(128, 192, 192)); //2
            mSelectedColors.add(Color.rgb(116, 160, 167)); //3
            mSelectedColors.add(Color.rgb(98, 154, 155)); //4
            mSelectedColors.add(Color.rgb(22, 89, 110)); //5
            mSelectedColors.add(Color.rgb(197, 60, 118)); //6
            mSelectedColors.add(Color.rgb(234, 129, 0)); //7
            mSelectedColors.add(Color.rgb(234, 227, 100)); //8
            mSelectedColors.add(Color.rgb(86, 147, 114)); //9
            mSelectedColors.add(Color.rgb(33, 174, 189)); //10
            mSelectedColors.add(Color.rgb(44, 79, 108)); //11
            mSelectedColors.add(Color.rgb(129, 145, 104)); //12
            mSelectedColors.add(Color.rgb(197, 164, 142)); //13
            mSelectedColors.add(Color.rgb(171, 114, 114)); //14
            mSelectedColors.add(Color.rgb(259, 28, 60)); //15
            mSelectedColors.add(Color.rgb(173, 17, 62)); //16
            mSelectedColors.add(Color.rgb(235, 82, 0)); //17
            mSelectedColors.add(Color.rgb(225, 179, 0)); //18
            mSelectedColors.add(Color.rgb(86, 130, 11)); //19
            mSelectedColors.add(Color.rgb(159, 80, 33)); //20
            mSelectedColors.add(Color.rgb(172, 235, 120)); //21
            mSelectedColors.add(Color.rgb(235, 227, 120)); //22
            mSelectedColors.add(Color.rgb(235, 188, 120)); //23
            mSelectedColors.add(Color.rgb(120, 214, 234)); //14
            mSelectedColors.add(Color.rgb(235, 120, 137)); //25
        }
        return mSelectedColors;
    }

}
