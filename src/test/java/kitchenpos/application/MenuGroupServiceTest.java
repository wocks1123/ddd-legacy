package kitchenpos.application;

import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceTest {

    @InjectMocks
    private MenuGroupService menuGroupService;

    @Mock
    private MenuGroupRepository menuGroupRepository;

    @Nested
    @DisplayName("메뉴 그룹 등록")
    class RegisterGroupMenu {

        @Test
        @DisplayName("메뉴 그룹 등록 성공")
        void testRegisterGroupMenu() {
            // given
            final String name = "한마리메뉴";
            final MenuGroup request = createMenuGroup(name);
            given(menuGroupRepository.save(any())).willReturn(request);

            // when
            final MenuGroup result = menuGroupService.create(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("메뉴 그룹은 이름을 필수로 가진다.")
        void testNullOrEmptyName(final String name) {
            // given
            final MenuGroup request = createMenuGroup(name);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuGroupService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("그룹 메뉴 조회")
    class FindGroupMenus {

        @Test
        @DisplayName("등록된 모든 메뉴 그룹을 조회한다.")
        void testFindAllGroupMenus() {
            // given
            final MenuGroup menuGroup1 = createMenuGroup("MENU_GROUP_NAME_1");
            final MenuGroup menuGroup2 = createMenuGroup("MENU_GROUP_NAME_2");
            given(menuGroupRepository.findAll()).willReturn(List.of(menuGroup1, menuGroup2));

            // when
            final List<MenuGroup> result = menuGroupService.findAll();

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(MenuGroup::getName)
                    .containsExactly(menuGroup1.getName(), menuGroup2.getName());
        }
    }

    private MenuGroup createMenuGroup(final String name) {
        final MenuGroup menuGroup = new MenuGroup();
        menuGroup.setId(UUID.randomUUID());
        menuGroup.setName(name);
        return menuGroup;
    }

}
