package nextstep.subway.line.application;

import nextstep.subway.line.application.exception.LineDuplicatedException;
import nextstep.subway.line.application.exception.LineNotFoundException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LineQueryServiceTest {
    private LineQueryService lineQueryService;

    @Mock
    private LineRepository lineRepository;
    private Line line1;
    private Line line2;

    @BeforeEach
    void setUp() {
        lineQueryService = new LineQueryService(lineRepository);
        line1 = new Line("1호선", "blue");
        line2 = new Line("2호선", "green");
    }

    @DisplayName("지하철 목록 조회를 하면 존재하는 모든 노선들을 리턴한다.")
    @Test
    void findAllLines() {
        //given
        when(lineRepository.findAll()).thenReturn(Arrays.asList(line1, line2));

        //when
        List<LineResponse> actual = lineQueryService.findAllLines();

        //then
        assertThat(actual).containsAll(Arrays.asList(LineResponse.of(line1), LineResponse.of(line2)));
    }

    @DisplayName("노선 ID를 요청하면 ID에 맞는 노선을 리턴한다.")
    @Test
    void findLine() {
        //given
        when(lineRepository.findById(anyLong())).thenReturn(Optional.of(line1));

        //when
        LineResponse actual = lineQueryService.findLine(anyLong());

        //then
        assertThat(actual.getName()).isEqualTo(line1.getName());
        assertThat(actual.getColor()).isEqualTo(line1.getColor());
    }

    @DisplayName("요청한 노선 ID가 존재하지 않는다면 예외를 발생시킨다.")
    @Test
    void findLineNotFoundException() {
        //given
        when(lineRepository.findById(anyLong())).thenReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> lineQueryService.findLine(anyLong()))
                .isInstanceOf(LineNotFoundException.class)
                .hasMessage(LineQueryService.LINE_ID_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @DisplayName("중복된 이름이 존재한다면 예외를 발생시킨다.")
    @Test
    void checkDuplicatedLineName() {
        //given
        when(lineRepository.findByName(anyString())).thenReturn(Optional.of(line1));

        //when
        assertThatThrownBy(() -> lineQueryService.checkDuplicatedLineName(anyString()))
                .isInstanceOf(LineDuplicatedException.class)
                .hasMessage(LineQueryService.LINE_NAME_DUPLICATED_EXCEPTION_MESSAGE);
    }
}