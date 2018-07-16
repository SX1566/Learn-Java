# [gftaxi-traffic-accident](https://gitee.com/gftaxi/gftaxi-traffic-accident) changelog

## 0.1.1 2018-07-16 19:30

- 跨域请求添加允许 Content-Type 请求头，修正提交报案的跨域权限问题
- 优化从邮件报案获取邮件内容的方法，自动去除 html 标记
- 增加收取报案邮件的缓存处理，避免重复将已收取的邮件转换为 DTO
- 修正视图和表单事故状态的返回值
- 修正新建的事故报案状态为"已登记"
- 修改事故报案 submit 接口返回事故编号和报案时间并优化代码

## 0.1.0 2018-07-16 11:00

- 事故报案的系统报案和邮件报案