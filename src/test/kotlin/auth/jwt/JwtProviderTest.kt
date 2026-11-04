package auth.jwt

import auth.support.JwtTestSupport
import auth.support.TestDataBuilder
import blog.vans_story_be.domain.auth.jwt.TokenType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe

/**
 * JwtProvider 단위 테스트 — 실제 키로 서명·검증한다.
 * 핵심: Access/Refresh 토큰이 typ 클레임으로 구분되어 서로의 자리에서 통과하지 않아야 한다.
 */
class JwtProviderTest : DescribeSpec({

    val jwtProvider = JwtTestSupport.jwtProvider()
    val authentication = JwtTestSupport.authentication(TestDataBuilder.TEST_EMAIL, "ROLE_ADMIN")

    fun secondsUntilExpiry(token: String): Long =
        (JwtTestSupport.parseClaims(token).expiration.time - System.currentTimeMillis()) / 1000

    describe("generateAccessToken은") {
        val token = jwtProvider.generateAccessToken(authentication)
        val claims = JwtTestSupport.parseClaims(token)

        it("typ=access와 sub, auth 클레임을 넣는다") {
            claims["typ"] shouldBe "access"
            claims.subject shouldBe TestDataBuilder.TEST_EMAIL
            claims["auth"] shouldBe "ROLE_ADMIN"
        }

        it("Access 유효기간으로 만료를 설정한다") {
            secondsUntilExpiry(token) shouldBeInRange
                (JwtTestSupport.ACCESS_VALIDITY - 60)..JwtTestSupport.ACCESS_VALIDITY
        }
    }

    describe("generateRefreshToken은") {
        val token = jwtProvider.generateRefreshToken(authentication)
        val claims = JwtTestSupport.parseClaims(token)

        it("typ=refresh와 sub, auth 클레임을 넣는다") {
            claims["typ"] shouldBe "refresh"
            claims.subject shouldBe TestDataBuilder.TEST_EMAIL
            claims["auth"] shouldBe "ROLE_ADMIN"
        }

        it("Refresh 유효기간으로 만료를 설정한다") {
            secondsUntilExpiry(token) shouldBeInRange
                (JwtTestSupport.REFRESH_VALIDITY - 60)..JwtTestSupport.REFRESH_VALIDITY
        }
    }

    describe("validateToken은") {
        val accessToken = jwtProvider.generateAccessToken(authentication)
        val refreshToken = jwtProvider.generateRefreshToken(authentication)
        val legacyToken = JwtTestSupport.tokenWithType(null)
        val unknownTypeToken = JwtTestSupport.tokenWithType("id")

        context("ACCESS를 기대하면") {
            it("Access 토큰을 통과시킨다") {
                jwtProvider.validateToken(accessToken, TokenType.ACCESS) shouldBe true
            }
            it("Refresh 토큰을 거부한다") {
                jwtProvider.validateToken(refreshToken, TokenType.ACCESS) shouldBe false
            }
            it("typ 없는 레거시 토큰을 거부한다") {
                jwtProvider.validateToken(legacyToken, TokenType.ACCESS) shouldBe false
            }
            it("알 수 없는 typ을 거부한다") {
                jwtProvider.validateToken(unknownTypeToken, TokenType.ACCESS) shouldBe false
            }
        }

        context("REFRESH를 기대하면") {
            it("Refresh 토큰을 통과시킨다") {
                jwtProvider.validateToken(refreshToken, TokenType.REFRESH) shouldBe true
            }
            it("Access 토큰을 거부한다") {
                jwtProvider.validateToken(accessToken, TokenType.REFRESH) shouldBe false
            }
            it("typ 없는 레거시 토큰을 거부한다") {
                jwtProvider.validateToken(legacyToken, TokenType.REFRESH) shouldBe false
            }
        }

        context("allowMissingType=true이면") {
            it("typ 없는 레거시 토큰만 추가로 통과시킨다") {
                jwtProvider.validateToken(legacyToken, TokenType.REFRESH, allowMissingType = true) shouldBe true
            }
            it("typ이 다른 토큰은 여전히 거부한다") {
                jwtProvider.validateToken(accessToken, TokenType.REFRESH, allowMissingType = true) shouldBe false
                jwtProvider.validateToken(unknownTypeToken, TokenType.REFRESH, allowMissingType = true) shouldBe false
            }
        }

        it("만료된 토큰은 typ이 맞아도 거부한다") {
            val expired = JwtTestSupport.tokenWithType("access", validityInSeconds = -10)
            jwtProvider.validateToken(expired, TokenType.ACCESS) shouldBe false
        }
    }
})
