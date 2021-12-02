package nextstep.subway.line;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.StationApiRequests;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("구간 추가 기능")
public class SectionInsertAcceptanceTest extends AcceptanceTest {

    private StationResponse 강남역;
    private StationResponse 잠실역;
    private Long 서울2호선;
    private int 기존노선길이 = 10;

    @BeforeEach
    void setUpSectionAcceptanceTest() {
        super.setUp();

        강남역 = StationApiRequests.지하철_역_생성됨("강남역");
        잠실역 = StationApiRequests.지하철_역_생성됨("잠실역");
        LineRequest lineRequest = new LineRequest("2호선", "bg-red-600", 강남역.getId(), 잠실역.getId(), 기존노선길이);
        서울2호선 = LineHelper.지하철_노선_등록되어_있음(lineRequest);
    }

    @DisplayName("역 사이에 구간을 등록한다.")
    @Test
    void addSection() {
        // given
        // 지하철 역 생성됨
        // 지하철 노선 생성됨
        StationResponse 삼성역 = StationApiRequests.지하철_역_생성됨("삼성역");
        SectionRequest sectionRequest = new SectionRequest(강남역.getId(), 삼성역.getId(), 4);

        // when
        // 노선 구간 등록 요청
        ExtractableResponse<Response> response = SectionApiRequests.지하철_구간_등록_요청(서울2호선, sectionRequest);

        // then
        // 노선에 구간 등록됨.
        지하철_노선_구간_등록됨(response);
        지하철_노선에_등록된_역_확인(서울2호선,"강남역","삼성역","잠실역");
    }

    @DisplayName("새로운 구간의 역을 상행 종점으로 등록한다.")
    @Test
    void addUpSection() {
        // given
        // 지하철 역 생성됨
        // 지하철 노선 생성됨
        StationResponse 사당역 = StationApiRequests.지하철_역_생성됨("사당역");
        SectionRequest sectionRequest = new SectionRequest(사당역.getId(), 강남역.getId(), 7);


        // when
        // 노선 구간 등록 요청
        ExtractableResponse<Response> response = SectionApiRequests.지하철_구간_등록_요청(서울2호선, sectionRequest);

        // then
        // 노선에 구간 등록됨.
        지하철_노선_구간_등록됨(response);
        지하철_노선에_등록된_역_확인(서울2호선,"사당역","강남역","잠실역");
    }

    @DisplayName("새로운 구간의 역을 하행 종점으로 등록한다.")
    @Test
    void addDownSection() {
        // given
        // 지하철 역 생성됨
        // 지하철 노선 생성됨
        StationResponse 강변역 = StationApiRequests.지하철_역_생성됨("강변역");
        SectionRequest sectionRequest = new SectionRequest(잠실역.getId(), 강변역.getId(), 7);


        // when
        // 노선 구간 등록 요청
        ExtractableResponse<Response> response = SectionApiRequests.지하철_구간_등록_요청(서울2호선, sectionRequest);

        // then
        // 노선에 구간 등록됨.
        지하철_노선_구간_등록됨(response);
        지하철_노선에_등록된_역_확인(서울2호선,"강남역","잠실역","강변역");
    }

    @DisplayName("기존 역 사이 길이보다 크거나 같은 구간을 등록한다.")
    @Test
    void overDistance() {
        // given
        // 지하철 역 생성됨
        // 지하철 노선 생성됨
        StationResponse 삼성역 = StationApiRequests.지하철_역_생성됨("삼성역");
        SectionRequest sectionRequest = new SectionRequest(강남역.getId(), 삼성역.getId(), 20);

        // when
        // 노선 구간 등록 요청
        ExtractableResponse<Response> response = SectionApiRequests.지하철_구간_등록_요청(서울2호선, sectionRequest);

        // then
        // 노선에 구간 등록됨.
        지하철_노선_구간_실패함(response, "새 노선의 길이가 기존 노선의 길이보다 작아야 합니다.");
    }

    @DisplayName("이미 노선에 등록된 구간을 등록한다.")
    @Test
    void existsSection() {
        // given
        // 지하철 역 생성됨
        // 지하철 노선 생성됨
        SectionRequest sectionRequest = new SectionRequest(강남역.getId(), 잠실역.getId(), 10);

        // when
        // 노선 구간 등록 요청
        ExtractableResponse<Response> response = SectionApiRequests.지하철_구간_등록_요청(서울2호선, sectionRequest);

        // then
        // 노선에 구간 등록됨.
        지하철_노선_구간_실패함(response,"이미 등록된 구간입니다.");
    }

    @DisplayName("상행역과 하행역 둘 중 하나도 노선에 등록되어있지 않은 구간을 등록한다.")
    @Test
    void notExistsStationInLine() {
        // given
        // 지하철 역 생성됨
        // 지하철 노선 생성됨
        StationResponse 홍대입구역 = StationApiRequests.지하철_역_생성됨("홍대입구역");
        StationResponse 신촌역 = StationApiRequests.지하철_역_생성됨("신촌역");
        SectionRequest sectionRequest = new SectionRequest(홍대입구역.getId(), 신촌역.getId(), 10);

        // when
        // 노선 구간 등록 요청
        ExtractableResponse<Response> response = SectionApiRequests.지하철_구간_등록_요청(서울2호선, sectionRequest);

        // then
        // 노선에 구간 등록됨.
        지하철_노선_구간_실패함(response,"상행역과 하행역 둘중 하나는 노선에 등록되어 있어야 합니다.");
    }

    private void 지하철_노선_구간_실패함(ExtractableResponse<Response> response, String message) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.jsonPath().getObject("message", String.class)).isEqualTo(message);
    }

    private void 지하철_노선_구간_등록됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_노선에_등록된_역_확인(Long lineId, String... names) {
        ExtractableResponse<Response> response = LineApiRequests.지하철_노선_조회_요청(lineId);
        LineResponse result = response.as(LineResponse.class);
        List<String> savedStationNames = result.getStations()
                .stream()
                .map(stationResponse -> stationResponse.getName())
                .collect(Collectors.toList());
        assertThat(savedStationNames).containsExactly(names);
    }

}
