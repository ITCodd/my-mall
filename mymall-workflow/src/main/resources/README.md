#什么是会签?
>会签是指一件事情的决断，需要所有的人都通过，该事件才允许通过；
#什么是或签？
>或签是指一件事情的决断，只需要其中一个人通过，该事件就允许通过；
#参数说明
完成条件涉及以下几个参数，解释如下：

nrOfInstances 实例总数

nrOfActiveInstances 当前还没有完成的实例

nrOfCompletedInstances 已经完成的实例个数

完成条件配置以下信息：

会签完成条件：${nrOfCompletedInstances/nrOfInstances >= 1.0 }

或签完成条件：${nrOfCompletedInstances/nrOfInstances > 0 }


工作流的几篇博文参考
https://blog.csdn.net/zhangpan_soft/article/details/78761937
https://blog.csdn.net/sinat_28690417/article/details/76811751?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase
https://blog.csdn.net/u013414218/article/details/84252914?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-12.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-12.nonecase
https://blog.csdn.net/u013899461/article/details/106086937?utm_medium=distribute.pc_relevant.none-task-blog-baidujs-3