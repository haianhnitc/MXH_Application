Prompt: DummyJSON là gì ? Giải thích ngắn gọn cho 1 newbie hiểu nhanh về DummyJSON thông qua tài liệu sau (paste tài liệu)

Prompt: "Hilt là gì? giải thích về Hilt cơ bản, có thể dùng liên hệ tới 1 số thư viện trong Flutter để giải thích rõ giúp tôi hiểu hơn"

Prompt: "Dựa trên tài liệu DummyJSON, hãy phân tích cấu trúc JSON trả về của endpoint /users và /posts. Hãy viết các Data Class trong Kotlin sử dụng thư viện Gson hoặc Kotlin Serialization. Lưu ý: Hãy bao gồm cả các class bọc ngoài (Wrapper) cho trường hợp có phân trang (chứa các trường như limit, skip, total). + (paste yêu cầu của app)"

Prompt: "Tôi đang xây dựng App bằng Kotlin và Jetpack Compose. Hãy giải thích cho tôi luồng đi của dữ liệu từ Retrofit (API) -> Room (Cache) -> Repository -> ViewModel -> Compose UI. Hãy viết một đoạn code mẫu nhỏ minh họa luồng này."

Promt: "Hãy đóng vai một Senior Android Developer, giải thích cho tôi luồng đi của dữ liệu từ khi nhận được `UserResponse` từ API, đi qua `UserMapper` để trở thành `UserEntity` và được lưu bởi `UserDao`."

Promt: "Giải thích chi tiết về TagConverter một cách dễ tiếp cận nhất"

Prompt: "Tôi có một Entity trong Room chứa một trường kiểu `List<String>`. Hãy viết một `TagConverter` sử dụng thư viện Gson để xử lý trường này, đảm bảo xử lý được trường hợp dữ liệu null hoặc rỗng."

Prompt: (paste yêu cầu của app) + "Dựa trên yêu cầu hệ thống quản lý User và Post (sử dụng DummyJSON), hãy phân tích và thiết kế Database Schema cho Room. Liệt kê các Entity cần thiết, các trường dữ liệu và mối quan hệ giữa User và Post. Xuất ra code Kotlin Data Class".

Prompt:"Với Yêu cầu của dự án, các đầu API cần cho dự án của tôi bao gồm: Lấy danh sách user và post có phân trang, lấy thông tin chi tiết user, post và tìm kiếm user, post. Ngoài những đầu API trên thì tôi cần những đầu API nào?"

Prompt: "Xây dựng DummyJsonApi instance với retrofit sao cho áp dụng được hilt module theo đúng yêu cầu của dự án"

Prompt: "Việc xây dựng DummyJsonApi giúp không cần viết http request thủ công nhưng tôi thấy nó không ngắn hơn là bao nhiêu so với việc gõ http request, vậy lợi ích của việc tạo Interface DummyJsonApi là gì?"

Prompt: "Trong nghiệp vụ của dự án có yêu cầu list Post cho từng user nữa vì trong trang chi tiết user có hiển thị, vậy có thể tạo chung 1 hàm để có thể truyền hoặc không truyền tham số userID không? Cho tôi phương án tối ưu nhất"

Prompt: "Tại sao tôi không thể thêm dependecies cho kilt và kapt vào gradle của projectlevel ?"

Prompt: "Tạo repository layer và ViewModel để nối repository với UI (Hiện tại chưa có thiết kế UI -> đề xuất mô tả bằng lời các màn tôi cần tạo để phù hợp với yêu cầu của dự án) + paste yêu cầu của app"

Prompt: "Có nên tạo 1 file chuyên để xử lí trạng thái khi gọi API cho đồng bộ không? Có những trạng thái nào cần phải bắt khi gọi API từ dummy?"

Prompt: "Tại sao phải bắt trạng thái loading , có thể dùng 1 biến có thể thay đổi trạng thái để theo dõi thay vì tạo thêm Loading cho file xử lí trạng thái được không ? có cần xử lí các trạng thái khác về API như 500, 404,.... hay không ? Giải thích chi tiết lợi ích mang lại khi tạo thêm state loading"

Prompt: "thực hiện tạo navigation setup và tạo tất cả các screen theo yêu cầu sao cho thỏa mãn các yêu cầu dự án đồng thời tham chiếu vào các đầu api, các hàm trong repository của tôi đã tạo để thực hiện tạo giao diện phù hợp. Tham khảo màn hình quản lí 1 danh sách các item cơ bản như trong ảnh. + paste yêu cầu app + paste ảnh minh họa màn userlist"

Prompt: "Việc tạo thủ công singleton cho AppDatabase là không cần thiết, hilt đã tự tạo rồi, ngoài ra bạn đang set tên của database trong DatabaseModule và AppDatabase khác nhau, nó có thể gây ra lỗi cho dự án của tôi. Giúp tôi xem xét đề xuất trên, chỉ rõ việc hilt đã tạo singleton tự động cho AppDatabase ở vị trí nào."

Prompt: "trong xem chi tiết user thì có hiển thị list post, tuy nhiên hiện tại listpost của user chưa được áp dụng phân trang và loadmore khi kéo xuống dưới, litmit là 20 đi. Giúp tôi chỉnh sửa sao cho phù hợp với logic trên"

Prompt: "Paste yêu cầu app + dựa vào tài liệu và toàn bộ code đã viết cho dự án hiện tại, hãy giúp tôi thiết kế các file test cho Repository và ViewModel, với những case khó có thể viết test thì hãy bỏ qua và list ra để tôi có thể tự test thủ công. Sau đó tiến hành chạy thử và kiểm tra lại các đoạn mã đã viết cho các file test."
