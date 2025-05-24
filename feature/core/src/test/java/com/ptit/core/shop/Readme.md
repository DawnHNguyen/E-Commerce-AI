# Unit Test cho Chức năng Quản lý Cửa hàng và Sản phẩm

Tài liệu này cung cấp tổng quan về các unit test được triển khai cho chức năng quản lý cửa hàng và sản phẩm trong ứng dụng.

## Tổng quan về Phạm vi Test

Các bài test bao gồm các thành phần và chức năng sau:

1.  **ShopViewModel**: Quản lý việc tạo, cập nhật và lấy thông tin chi tiết cửa hàng.
2.  **ShopUiModel**: Đại diện cho trạng thái UI cho các màn hình liên quan đến cửa hàng (được định nghĩa trong `ShopViewModel.kt`).
3.  **ProductViewModel**: Xử lý việc tạo, cập nhật, xóa sản phẩm và lấy danh sách sản phẩm/danh mục.
4.  **(Các trạng thái UI hoặc lớp tiện ích khác nếu có)**

## Các Lớp Test

### 1. ShopViewModelTest

Kiểm tra chức năng của ViewModel bao gồm:

* Lấy thông tin chi tiết cửa hàng (`WorkspaceMyShopDetails`).
* Cập nhật thông tin chi tiết cửa hàng (tên, mô tả, địa chỉ, ảnh đại diện) (`updateShopDetails`).
    * Validate dữ liệu đầu vào (dựa trên đặc tả PDF):
        * Tên cửa hàng: Tối đa 50 ký tự, không chứa ký tự đặc biệt[cite: 5].
        * Mô tả cửa hàng: Tối đa 200 ký tự[cite: 5].
        * Địa chỉ cửa hàng: Tối đa 50 ký tự[cite: 5].
    * Xử lý cập nhật thành công.
    * Xử lý lỗi trong quá trình cập nhật.
* Tải lên ảnh đại diện cửa hàng (`uploadShopImage`).
    * Xử lý tải lên thành công.
    * Xử lý lỗi trong quá trình tải lên.
* Cập nhật trạng thái UI (`ShopUiModel`, `shopDetailsState`, `updateShopState`, `uploadImageState`) dựa trên phản hồi từ API.

**Các Trường hợp Test Chính:**

* `WorkspaceMyShopDetails_success_updatesUiModelAndShopDetailsState`: Kiểm tra việc lấy thông tin cửa hàng thành công cập nhật `uiModel` và `shopDetailsState`.
* `WorkspaceMyShopDetails_error_updatesShopDetailsStateWithError`: Kiểm tra việc xử lý lỗi khi lấy thông tin cửa hàng.
* `updateShopDetails_validInput_callsRepositoryAndUpdateSuccess`: Kiểm tra cập nhật thông tin cửa hàng với dữ liệu hợp lệ, gọi repository và cập nhật trạng thái thành công.
* `updateShopDetails_invalidName_tooLong_doesNotCallRepository`: Kiểm tra tên cửa hàng quá dài.
* `updateShopDetails_invalidName_specialCharacters_doesNotCallRepository`: Kiểm tra tên cửa hàng chứa ký tự đặc biệt.
* `updateShopDetails_invalidDescription_tooLong_doesNotCallRepository`: Kiểm tra mô tả cửa hàng quá dài.
* `updateShopDetails_invalidAddress_tooLong_doesNotCallRepository`: Kiểm tra địa chỉ cửa hàng quá dài.
* `updateShopDetails_emptyRequiredField_name_doesNotCallRepository`: Kiểm tra tên cửa hàng rỗng.
* `updateShopDetails_emptyRequiredField_address_doesNotCallRepository`: Kiểm tra địa chỉ cửa hàng rỗng.
* `updateShopDetails_apiError_updatesUpdateShopStateWithError`: Kiểm tra xử lý lỗi API khi cập nhật.
* `uploadShopImage_success_updatesUploadImageStateWithUrl`: Kiểm tra tải ảnh đại diện thành công, cập nhật `uploadImageState` với URL.
* `uploadShopImage_error_updatesUploadImageStateWithError`: Kiểm tra xử lý lỗi khi tải ảnh.
* `initialState_isCorrect`: Kiểm tra trạng thái khởi tạo của `ShopUiModel` và các `StateFlow` khác.

### 2. ProductViewModelTest

Kiểm tra chức năng của ViewModel bao gồm:

* Lấy danh sách sản phẩm của cửa hàng (`WorkspaceProductList`).
* Lấy danh sách danh mục (`getCategories`).
* Lấy thông tin chi tiết của một sản phẩm cụ thể (cho việc chỉnh sửa) (`getProductDetails`).
* Tạo sản phẩm mới (`createProduct`).
    * Validate dữ liệu đầu vào (dựa trên đặc tả PDF):
        * Tên sản phẩm: Tối đa 50 ký tự, không rỗng[cite: 15].
        * Mô tả sản phẩm: Tối đa 200 ký tự[cite: 15].
        * Giá (VNĐ): Nguyên > 0, tối đa 18 chữ số[cite: 15].
        * Giá gốc (VNĐ): Nguyên > 0 (nếu có), tối đa 18 chữ số và phải lớn hơn Giá nếu Giá > 0[cite: 16].
        * Số lượng: Nguyên > 0, tối đa 18 chữ số[cite: 16].
        * Danh mục: Bắt buộc chọn[cite: 16].
        * Hình ảnh: Từ 2 đến 5 hình ảnh, định dạng JPG hoặc PNG[cite: 16]. (Mã nguồn hiện tại cho phép chọn 1 ảnh chính và danh sách ảnh phụ, tổng cộng tối đa 5).
    * Xử lý tạo sản phẩm thành công.
    * Xử lý lỗi trong quá trình tạo sản phẩm.
* Cập nhật sản phẩm hiện có (`updateProduct`).
    * Validate tương tự như tạo mới.
    * Xử lý cập nhật thành công.
    * Xử lý lỗi trong quá trình cập nhật.
* Xóa sản phẩm (`deleteProduct`).
    * Xử lý xóa thành công.
    * Xử lý lỗi trong quá trình xóa.
* Tải lên hình ảnh sản phẩm (`uploadProductImages`).
    * Xử lý tải lên thành công.
    * Xử lý lỗi trong quá trình tải lên.
* Cập nhật trạng thái UI cho danh sách sản phẩm, chi tiết sản phẩm, danh mục và các thao tác lưu/xóa.

**Các Trường hợp Test Chính:**

* `WorkspaceProductList_success_updatesProductListStateAndProductListFlow`: Kiểm tra lấy danh sách sản phẩm thành công.
* `WorkspaceProductList_error_updatesProductListStateWithError`: Kiểm tra xử lý lỗi khi lấy danh sách sản phẩm.
* `getCategories_success_updatesCategoryListState`: Kiểm tra lấy danh mục thành công.
* `getCategories_error_updatesCategoryListStateWithError`: Kiểm tra xử lý lỗi khi lấy danh mục.
* `getProductDetails_success_updatesProductDetailsState`: Kiểm tra lấy chi tiết sản phẩm thành công.
* `getProductDetails_error_updatesProductDetailsStateWithError`: Kiểm tra xử lý lỗi khi lấy chi tiết sản phẩm.
* `createProduct_validInput_uploadsImagesAndCallsRepositorySuccess`: Tạo sản phẩm với dữ liệu hợp lệ.
* `createProduct_invalidName_empty_updatesSaveProductStateWithError`: Tên sản phẩm rỗng.
* `createProduct_invalidName_tooLong_updatesSaveProductStateWithError`: Tên sản phẩm quá dài.
* `createProduct_invalidPrice_notPositive_updatesSaveProductStateWithError`: Giá không dương.
* `createProduct_invalidPrice_tooLarge_updatesSaveProductStateWithError`: Giá quá lớn (vượt 18 chữ số - khó test trực tiếp giới hạn chữ số, tập trung vào kiểu dữ liệu và giá trị).
* `createProduct_invalidPriceBeforeDiscount_lessThanPrice_updatesSaveProductStateWithError`: Giá gốc nhỏ hơn giá bán.
* `createProduct_invalidQuantity_notPositive_updatesSaveProductStateWithError`: Số lượng không dương.
* `createProduct_invalidCategory_empty_updatesSaveProductStateWithError`: Chưa chọn danh mục.
* `createProduct_invalidImageCount_tooFew_updatesSaveProductStateWithError`: Số lượng ảnh < 2 (nếu theo đặc tả PDF, mã nguồn hiện tại có thể khác).
* `createProduct_invalidImageCount_tooMany_updatesSaveProductStateWithError`: Số lượng ảnh > 5.
* `createProduct_apiError_updatesSaveProductStateWithError`: Lỗi API khi tạo sản phẩm.
* `updateProduct_validInput_callsRepositorySuccess`: Cập nhật sản phẩm với dữ liệu hợp lệ.
* `updateProduct_apiError_updatesSaveProductStateWithError`: Lỗi API khi cập nhật sản phẩm.
* `uploadProductImages_success_updatesUploadImagesStateWithUrls`: Tải ảnh sản phẩm thành công.
* `uploadProductImages_error_updatesUploadImagesStateWithError`: Lỗi khi tải ảnh sản phẩm.
* `deleteProduct_success_updatesDeleteProductStateAndRefreshesList`: Xóa sản phẩm thành công và làm mới danh sách.
* `deleteProduct_error_updatesDeleteProductStateWithError`: Lỗi khi xóa sản phẩm.
* `initialStates_areCorrect`: Kiểm tra các trạng thái khởi tạo.

## Phương pháp Test

1.  **Chiến lược Mocking:**
    * Sử dụng Mockito để mock các `ShopRepository`, `ProductRepository`, và `FileUploadRepository`.
    * Các phụ thuộc Android-specific (như `Context` cho `FileUploadRepositoryImpl`) sẽ được Hilt cung cấp hoặc mock nếu cần thiết trong môi trường test cụ thể (ví dụ: `ApplicationContext` cho `FileUploadRepositoryImpl` nên được Hilt xử lý trong test integration, unit test ViewModel nên tránh mock `Context`).
2.  **Test Bất đồng bộ:**
    * Sử dụng Kotlin Coroutines Test (`runTest`, `StandardTestDispatcher`, `advanceUntilIdle`) để xử lý các hoạt động bất đồng bộ trong ViewModels.
    * Sử dụng Turbine để kiểm tra các `StateFlow` emissions.
3.  **Cách ly Test (Test Isolation):**
    * Đảm bảo các ViewModels có thể được test bằng cách inject các phụ thuộc (dependencies).
    * Tập trung vào unit testing logic bên trong mỗi ViewModel và các lớp trạng thái UI liên quan.

## Phạm vi Test (Mục tiêu)

* **ShopViewModel Logic:** ~90% coverage.
* **ProductViewModel Logic:** ~90% coverage.

## Chạy Tests

Để chạy tất cả các test cho quản lý cửa hàng và sản phẩm:

```bash
./gradlew :feature:core:testDebugUnitTest --tests "com.ptit.core.shop.*"
./gradlew :feature:core:testDebugUnitTest --tests "com.ptit.core.product.*"